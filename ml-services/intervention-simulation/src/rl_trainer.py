"""
MANO Component 3: PPO Training Logic
Trains the AI Doctor using Proximal Policy Optimization.
"""
import torch
import torch.nn as nn
import numpy as np
import sys
import os
from pathlib import Path

# --- SETUP PATHS ---
sys.path.insert(0, str(Path(__file__).parent))
config_path = os.path.join(os.getcwd(), 'ml-services',
                           'intervention-simulation', 'config')
sys.path.append(config_path)

try:
    from rl_config import config
    from rl_environment import MedicalEnvironment
    from rl_agent import ActorCritic
except ImportError:
    sys.exit(1)


class Memory:
    def __init__(self):
        self.actions_cat = []
        self.actions_cont = []
        self.states = []
        self.logprobs = []
        self.rewards = []
        self.is_terminals = []

    def clear(self):
        del self.actions_cat[:]
        del self.actions_cont[:]
        del self.states[:]
        del self.logprobs[:]
        del self.rewards[:]
        del self.is_terminals[:]


class PPOTrainer:
    def __init__(self):
        self.device = config.ppo.DEVICE
        self.env = MedicalEnvironment()

        # Initialize Policy
        self.policy = ActorCritic(
            config.env.STATE_DIM,
            config.env.NUM_INTERVENTIONS,
            config.ppo.HIDDEN_DIM,
            self.device
        )

        # Single Optimizer for both Actor and Critic
        self.optimizer = torch.optim.Adam(
            self.policy.parameters(),
            lr=config.ppo.LR_ACTOR
        )

        # Old Policy (Required for PPO Ratio calculation)
        self.policy_old = ActorCritic(
            config.env.STATE_DIM,
            config.env.NUM_INTERVENTIONS,
            config.ppo.HIDDEN_DIM,
            self.device
        )
        self.policy_old.load_state_dict(self.policy.state_dict())

        self.MseLoss = nn.MSELoss()

    def update(self, memory):
        # Monte Carlo estimate of state rewards
        rewards = []
        discounted_reward = 0
        for reward, is_terminal in zip(reversed(memory.rewards), reversed(memory.is_terminals)):
            if is_terminal:
                discounted_reward = 0
            discounted_reward = reward + (config.ppo.GAMMA * discounted_reward)
            rewards.insert(0, discounted_reward)

        # Normalizing the rewards
        rewards = torch.tensor(rewards, dtype=torch.float32).to(self.device)
        rewards = (rewards - rewards.mean()) / (rewards.std() + 1e-7)

        # Convert list to tensor
        old_states = torch.squeeze(torch.stack(
            memory.states)).detach().to(self.device)
        old_actions_cat = torch.squeeze(torch.stack(
            memory.actions_cat)).detach().to(self.device)
        old_actions_cont = torch.squeeze(torch.stack(
            memory.actions_cont)).detach().to(self.device)
        old_logprobs = torch.squeeze(torch.stack(
            memory.logprobs)).detach().to(self.device)

        # Optimize policy for K epochs
        for _ in range(config.ppo.K_EPOCHS):
            # Evaluating old actions and values
            logprobs, state_values, dist_entropy = self.policy.evaluate(
                old_states, old_actions_cat, old_actions_cont)

            # Finding the ratio (pi_theta / pi_theta__old)
            ratios = torch.exp(logprobs - old_logprobs)

            # Finding Surrogate Loss
            advantages = rewards - state_values.detach()
            surr1 = ratios * advantages
            surr2 = torch.clamp(ratios, 1-config.ppo.EPS_CLIP,
                                1+config.ppo.EPS_CLIP) * advantages

            # Final loss = Actor Loss + Critic Loss - Entropy Bonus
            loss = -torch.min(surr1, surr2) + 0.5*self.MseLoss(state_values,
                                                               rewards) - config.ppo.ENTROPY_COEFF*dist_entropy

            # Take Gradient Step
            self.optimizer.zero_grad()
            loss.mean().backward()
            self.optimizer.step()

        # Copy new weights to old policy
        self.policy_old.load_state_dict(self.policy.state_dict())


def train():
    print(f"\n🚀 STARTING PPO TRAINING ({config.ppo.DEVICE})")
    print(f"Goal: {config.ppo.MAX_EPISODES} Episodes")

    trainer = PPOTrainer()
    memory = Memory()

    time_step = 0
    running_reward = 0

    for i_episode in range(1, config.ppo.MAX_EPISODES+1):
        state = trainer.env.reset()
        current_ep_reward = 0

        for t in range(config.env.MAX_STEPS):
            time_step += 1

            # Run Policy (No Gradient Tracking)
            with torch.no_grad():
                action, _, _ = trainer.policy_old.act(state)

            # Execute Action
            state_n, reward, done, _ = trainer.env.step(action[0], action[1])

            # Save data for training
            memory.states.append(state)
            memory.actions_cat.append(torch.tensor(action[0]))
            memory.actions_cont.append(torch.tensor(action[1]))
            # Recalculate logprob inside update loop usually, but here we store for simplicity
            # Actually for PPO we need logprob of action under OLD policy
            with torch.no_grad():
                _, log_prob, _ = trainer.policy_old.act(
                    state)  # Re-run to get tensor logprob
            memory.logprobs.append(torch.tensor(log_prob))

            memory.rewards.append(reward)
            memory.is_terminals.append(done)

            state = state_n
            current_ep_reward += reward

            # Update PPO Agent
            if time_step % config.ppo.UPDATE_TIMESTEP == 0:
                trainer.update(memory)
                memory.clear()
                time_step = 0

            if done:
                break

        running_reward += current_ep_reward

        # Logging & Saving
        if i_episode % config.ppo.PRINT_INTERVAL == 0:
            avg_reward = running_reward / config.ppo.PRINT_INTERVAL
            print(f"Episode {i_episode} \t Avg Reward: {avg_reward:.2f}")
            running_reward = 0

            # Checkpoint
            torch.save(trainer.policy.state_dict(),
                       config.paths.BEST_AGENT_PATH)

    print("✅ TRAINING COMPLETE")


if __name__ == '__main__':
    train()
