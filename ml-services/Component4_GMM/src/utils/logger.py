"""
Logging System

Provides comprehensive logging for the entire system.

Author: [Your Name]
Date: [Current Date]
"""

import logging
import os
from datetime import datetime
from typing import Optional


class ManoCommunityLogger:
    """
    Custom logger for Component 4
    """

    def __init__(self,
                 name: str = "ManoCommunity",
                 log_dir: str = "logs",
                 log_level: str = "INFO"):
        """
        Initialize logger

        Parameters:
            name (str): Logger name
            log_dir (str): Directory for log files
            log_level (str): Logging level (DEBUG, INFO, WARNING, ERROR)
        """
        self.logger = logging.getLogger(name)
        self.logger.setLevel(getattr(logging, log_level.upper()))

        # Create log directory
        os.makedirs(log_dir, exist_ok=True)

        # Create formatters
        detailed_formatter = logging.Formatter(
            '%(asctime)s | %(levelname)-8s | %(name)s | %(funcName)s:%(lineno)d | %(message)s',
            datefmt='%Y-%m-%d %H:%M:%S'
        )

        simple_formatter = logging.Formatter(
            '%(asctime)s | %(levelname)-8s | %(message)s',
            datefmt='%H:%M:%S'
        )

        # File handler (detailed logs)
        log_filename = os.path.join(
            log_dir,
            f"mano_component4_{datetime.now().strftime('%Y%m%d')}.log"
        )

        file_handler = logging.FileHandler(log_filename, encoding='utf-8')
        file_handler.setLevel(logging.DEBUG)
        file_handler.setFormatter(detailed_formatter)

        # Console handler (simple logs)
        console_handler = logging.StreamHandler()
        console_handler.setLevel(logging.INFO)
        console_handler.setFormatter(simple_formatter)

        # Add handlers
        self.logger.addHandler(file_handler)
        self.logger.addHandler(console_handler)

        self.logger.info(f"Logger initialized: {name}")
        self.logger.info(f"Log file: {log_filename}")

    def debug(self, message: str):
        """Log debug message"""
        self.logger.debug(message)

    def info(self, message: str):
        """Log info message"""
        self.logger.info(message)

    def warning(self, message: str):
        """Log warning message"""
        self.logger.warning(message)

    def error(self, message: str, exc_info: bool = False):
        """Log error message"""
        self.logger.error(message, exc_info=exc_info)

    def critical(self, message: str, exc_info: bool = False):
        """Log critical message"""
        self.logger.critical(message, exc_info=exc_info)

    def log_data_processing(self,
                            n_users: int,
                            n_features: int,
                            processing_time: float):
        """Log data processing event"""
        self.logger.info(
            f"Data processed: {n_users} users, {n_features} features, "
            f"{processing_time:.2f}s"
        )

    def log_clustering(self,
                       n_clusters: int,
                       silhouette: float,
                       processing_time: float):
        """Log clustering event"""
        self.logger.info(
            f"Clustering complete: {n_clusters} clusters, "
            f"silhouette={silhouette:.3f}, {processing_time:.2f}s"
        )

    def log_community_creation(self, n_communities: int, avg_size: float):
        """Log community creation event"""
        self.logger.info(
            f"Communities created: {n_communities} communities, "
            f"avg_size={avg_size:.1f}"
        )

    def log_user_assignment(self, user_id: str, cluster: int, confidence: float):
        """Log user cluster assignment"""
        self.logger.debug(
            f"User assignment: {user_id} -> Cluster {cluster} "
            f"(confidence: {confidence:.1%})"
        )

    def log_error_with_context(self,
                               error_type: str,
                               error_message: str,
                               context: dict):
        """Log error with context"""
        self.logger.error(
            f"{error_type}: {error_message} | Context: {context}",
            exc_info=True
        )


# Testing code
if __name__ == "__main__":
    """
    Test the logger
    """
    print("Testing Logger Module")
    print("=" * 60)

    # Initialize logger
    logger = ManoCommunityLogger(log_level="DEBUG")

    # Test different log levels
    logger.debug("This is a debug message")
    logger.info("This is an info message")
    logger.warning("This is a warning message")
    logger.error("This is an error message")

    # Test specialized logging
    logger.log_data_processing(100, 5, 2.5)
    logger.log_clustering(5, 0.538, 5.2)
    logger.log_community_creation(11, 9.1)
    logger.log_user_assignment("USER_001", 2, 0.85)

    # Test error logging with context
    try:
        raise ValueError("Example error")
    except Exception as e:
        logger.log_error_with_context(
            "ValueError",
            str(e),
            {"user_id": "USER_123", "operation": "clustering"}
        )

    print("\n✓ Check logs/ directory for log file")
    print("=" * 60)