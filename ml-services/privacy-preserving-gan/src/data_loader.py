"""
Data Loader for Privacy-Preserving Mental Health Simulation System
This module handles loading and basic preprocessing of mental health datasets.
"""

# Import necessary libraries (like importing tools into your workshop)
import pandas as pd  # For handling CSV files and data tables
import numpy as np   # For mathematical operations and arrays
import os           # For operating system operations (file paths, etc.)
from pathlib import Path  # For handling file paths in a cross-platform way

class MentalHealthDataLoader:
    """
    A class to load and manage multiple mental health datasets
    for the Privacy-Preserving GAN component.
    
    Think of this class as a specialized data manager that knows how to:
    - Find your CSV files
    - Load them into memory
    - Give you information about them
    """
    
    def __init__(self, data_root_path):
        """
        This is the constructor - it runs when you create a new data loader.
        It sets up the basic settings and paths.
        
        Args:
            data_root_path (str): Path to the \'data\' folder in your project
        """
        # Convert the path string to a Path object (easier to work with)
        self.data_root = Path(data_root_path)
        
        # Create paths to specific subfolders
        # Where your original CSV files are
        self.raw_data_path = self.data_root / "raw"
        self.processed_data_path = self.data_root / \
            "processed"  # Where cleaned data will go
        
        # Create an empty dictionary to store all loaded datasets
        # Think of this as a filing cabinet with labeled drawers
        self.datasets = {}
        
        # Print information so you know what\'s happening
        print(f"Data loader initialized with root path: {self.data_root}")
        print(f"Raw data path: {self.raw_data_path}")
    
    def load_dass_dataset(self):
        """
        Load the Depression Anxiety Stress Scales (DASS) dataset.
        This is our largest and most important dataset (39,775 responses).
        """
        dass_data = None # Initialize dass_data to None
        try:  # Try to load the file, but be ready to handle errors
            # The DASS data is inside a folder, so we need to go into that folder first
            folder_path = self.raw_data_path / "Depression anxiety stress scales"
            file_path = folder_path / "Depression Anxiety Stress Scales Responses.csv"
            
            # Tell the user what we\'re doing
            print(f"Loading DASS dataset from: {file_path}")
            
            # Use pandas to read the CSV file into a DataFrame (like opening Excel file)
            dass_data = pd.read_csv(file_path, on_bad_lines='skip', sep='\t')
            
            # Print success message and basic information about the data
            print(f"DASS Dataset loaded successfully!")
            if dass_data is not None:
                print(f"Shape: {dass_data.shape}")  # Shows (rows, columns)
                
                # Show first 10 column names (so we don\'t overwhelm the screen)
                print(f"Columns: {list(dass_data.columns[:10])}...")
            
            # Store this dataset in our filing cabinet with the label \'dass\'
            self.datasets['dass'] = dass_data
            
            # Return the data so other parts of the program can use it
            return dass_data
            
        except FileNotFoundError:  # If the file doesn\'t exist
            print(f"Error: DASS dataset not found at {file_path}")
            return None  # Return nothing to indicate failure
            
        except Exception as e:  # If any other error happens
            print(f"Error loading DASS dataset: {e}")
            return None
    
    def load_tech_survey_2014(self):
        """
        Load the Mental Health in Tech Survey 2014 dataset.
        This provides STEM professional context (your target demographic).
        """
        try:  # Try to load, handle errors if they occur
            # Create path to the tech survey file (corrected filename)
            file_path = self.raw_data_path / "Mental Health in Tech Survey 2014.csv"
            
            # Tell user what we\'re doing
            print(f"Loading Tech Survey 2014 from: {file_path}")
            
            # Read the CSV file into a pandas DataFrame
            tech_data = pd.read_csv(file_path)
            
            # Print success information
            print(f"Tech Survey 2014 loaded successfully!")
            print(f"Shape: {tech_data.shape}")  # (rows, columns)
            print(f"Columns: {list(tech_data.columns)}")  # All column names
            
            # Store in our datasets filing cabinet with label \'tech_2014\'
            self.datasets['tech_2014'] = tech_data
            
            # Return the data
            return tech_data
            
        except FileNotFoundError:  # File not found error
            print(f"Error: Tech Survey 2014 not found at {file_path}")
            return None
            
        except Exception as e:  # Any other error
            print(f"Error loading Tech Survey 2014: {e}")
            return None
    
    def load_sleep_health_dataset(self):
        """
        Load the Sleep Health and Lifestyle dataset.
        This provides lifestyle factors that correlate with mental health.
        """
        try:  # Try to load, be ready for errors
            # Create path to sleep health file (this filename looks correct)
            file_path = self.raw_data_path / "Sleep_health_and_lifestyle_dataset.csv"
            
            # Tell user what\'s happening
            print(f"Loading Sleep Health dataset from: {file_path}")
            
            # Read CSV into DataFrame
            sleep_data = pd.read_csv(file_path)
            
            # Print success information
            print(f"Sleep Health dataset loaded successfully!")
            print(f"Shape: {sleep_data.shape}")  # (rows, columns)
            print(f"Columns: {list(sleep_data.columns)}")  # All column names
            
            # Store in datasets with label \'sleep_health\'
            self.datasets['sleep_health'] = sleep_data
            
            # Return the data
            return sleep_data
            
        except FileNotFoundError:  # File not found
            print(f"Error: Sleep Health dataset not found at {file_path}")
            return None
            
        except Exception as e:  # Other errors
            print(f"Error loading Sleep Health dataset: {e}")
            return None
    
    def get_dataset_summary(self):
        """
        Get a summary of all loaded datasets.
        This gives you an overview of what data you have available.
        """
        # Print a nice header
        print("\n" + "="*50)  # Creates a line of 50 equal signs
        print("DATASET SUMMARY")
        print("="*50)
        
        # Loop through each dataset in our filing cabinet
        for name, data in self.datasets.items():
            # Print information about each dataset
            print(f"\n{name.upper()}:")  # Dataset name in uppercase
            print(f"  - Shape: {data.shape}")  # (rows, columns)
            
            # Calculate memory usage in megabytes
            memory_mb = data.memory_usage(deep=True).sum() / 1024**2
            print(f"  - Memory usage: {memory_mb:.2f} MB")
            
            # Count total missing values across all columns
            missing_values = data.isnull().sum().sum()
            print(f"  - Missing values: {missing_values}")
        
        # Calculate total number of samples across all datasets
        total_samples = sum([data.shape[0] for data in self.datasets.values()])
        print(f"\nTotal samples across all datasets: {total_samples}")
    
    def load_all_basic_datasets(self):
        """
        Load all the basic datasets we\'ll use for initial GAN training.
        This is a convenience function that loads everything at once.
        """
        # Tell user what we\'re doing
        print("Loading all basic datasets...")
        
        # Call each individual loading function
        self.load_dass_dataset()      # Load DASS data
        self.load_tech_survey_2014()  # Load tech survey data
        self.load_sleep_health_dataset()  # Load sleep health data
        
        # Show summary of all loaded data
        self.get_dataset_summary()
        
        # Return the dictionary containing all datasets
        return self.datasets

# Test function to verify everything works
def test_data_loader():
    """
    Test function to verify the data loader works correctly.
    This function creates a data loader and tries to load all datasets.
    """
    # Set the path to your data folder - CORRECTED PATH
    # The script is being run from the project root, so the path to data is direct.
    data_path = "data"
    
    # Create a new instance of our MentalHealthDataLoader class
    loader = MentalHealthDataLoader(data_path)
    
    # Use the loader to load all datasets
    datasets = loader.load_all_basic_datasets()
    
    # Return the loaded datasets
    return datasets

# This special condition checks if this file is being run directly
# (not imported by another file)
if __name__ == "__main__":
    # Print a message to show we\'re testing
    print("Testing Mental Health Data Loader...")
    
    # Run the test function
    test_data_loader()