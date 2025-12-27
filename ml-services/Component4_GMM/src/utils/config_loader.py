"""
Configuration Loader

Loads and manages system configuration from YAML files.

Author: [Your Name]
Date: [Current Date]
"""

import yaml
import os
from typing import Any, Dict


class ConfigLoader:
    """
    Loads and provides access to configuration
    """

    def __init__(self, config_path: str = "config/config.yaml"):
        """
        Initialize config loader

        Parameters:
            config_path (str): Path to config file
        """
        if not os.path.exists(config_path):
            raise FileNotFoundError(f"Config file not found: {config_path}")

        with open(config_path, 'r', encoding='utf-8') as f:
            self.config = yaml.safe_load(f)

        print(f"✓ Configuration loaded from: {config_path}")

    def get(self, key: str, default: Any = None) -> Any:
        """
        Get configuration value using dot notation

        Parameters:
            key (str): Config key (e.g., 'clustering.n_components')
            default: Default value if key not found

        Returns:
            Configuration value
        """
        keys = key.split('.')
        value = self.config

        for k in keys:
            if isinstance(value, dict) and k in value:
                value = value[k]
            else:
                return default

        return value

    def get_all(self) -> Dict:
        """Get entire configuration"""
        return self.config

    def get_section(self, section: str) -> Dict:
        """
        Get entire configuration section

        Parameters:
            section (str): Section name (e.g., 'clustering')

        Returns:
            dict: Section configuration
        """
        return self.config.get(section, {})

    def print_config(self):
        """Print formatted configuration"""
        print("\n" + "=" * 60)
        print("SYSTEM CONFIGURATION")
        print("=" * 60)

        self._print_dict(self.config, indent=0)

    def _print_dict(self, d: dict, indent: int = 0):
        """Recursively print dictionary"""
        for key, value in d.items():
            if isinstance(value, dict):
                print("  " * indent + f"{key}:")
                self._print_dict(value, indent + 1)
            else:
                print("  " * indent + f"{key}: {value}")


# Testing code
if __name__ == "__main__":
    """
    Test the config loader
    """
    # Get the project root directory (Component4_GMM)
    SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
    PROJECT_ROOT = os.path.abspath(os.path.join(SCRIPT_DIR, "..", ".."))

    print("Testing Configuration Loader")
    print("=" * 60)

    # Load config
    config_path = os.path.join(PROJECT_ROOT, "config", "config.yaml")
    config = ConfigLoader(config_path)

    # Test getting values
    print("\n1. Getting specific values:")
    print(f"System name: {config.get('system.name')}")
    print(f"Number of clusters: {config.get('clustering.n_components')}")
    print(f"Feature columns: {config.get('data.feature_columns')}")
    print(f"Min community size: {config.get('community.min_size')}")

    # Test getting section
    print("\n2. Getting entire section:")
    clustering_config = config.get_section('clustering')
    print(f"Clustering config: {clustering_config}")

    # Print all config
    print("\n3. Full configuration:")
    config.print_config()

    print("\n" + "=" * 60)
    print("✓ ALL TESTS PASSED!")
    print("=" * 60)
