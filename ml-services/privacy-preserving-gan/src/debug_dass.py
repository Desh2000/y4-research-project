import pandas as pd
from pathlib import Path


def debug_dass_loader():
    # The script is being run from the project root, so the path to data is direct.
    data_root = Path("data")
    raw_data_path = data_root / "raw"

    folder_path = raw_data_path / "Depression anxiety stress scales"
    file_path = folder_path / "Depression Anxiety Stress Scales Responses.csv"

    print(f"Current working directory: {Path.cwd()}")
    print(f"Attempting to load DASS dataset from: {file_path}")

    try:
        # Try reading with default parameters first
        dass_data = pd.read_csv(file_path)
        print("Successfully loaded DASS with default parameters.")
        print(f"Shape: {dass_data.shape}")
        print(f"Columns: {list(dass_data.columns[:10])}...")

    except pd.errors.ParserError as e:
        print(f"ParserError encountered: {e}")
        print("Attempting to load with different parameters...")

        try:
            # Try reading with a different separator, e.g., semicolon
            dass_data = pd.read_csv(file_path, sep=";")
            print("Successfully loaded DASS with semicolon separator.")
            print(f"Shape: {dass_data.shape}")
            print(f"Columns: {list(dass_data.columns[:10])}...")
        except Exception as e_semicolon:
            print(f"Failed to load with semicolon separator: {e_semicolon}")

        try:
            # Try reading with 'engine=python' for more robust parsing
            dass_data = pd.read_csv(file_path, engine='python')
            print("Successfully loaded DASS with python engine.")
            print(f"Shape: {dass_data.shape}")
            print(f"Columns: {list(dass_data.columns[:10])}...")
        except Exception as e_python:
            print(f"Failed to load with python engine: {e_python}")

        try:
            # Try reading with 'engine=python' and a different delimiter (tab)
            dass_data = pd.read_csv(file_path, engine='python', sep='\t')
            print("Successfully loaded DASS with tab separator.")
            print(f"Shape: {dass_data.shape}")
            print(f"Columns: {list(dass_data.columns[:10])}...")
        except Exception as e_tab:
            print(f"Failed to load with tab separator: {e_tab}")

        try:
            # Try reading with error handling for bad lines
            dass_data = pd.read_csv(file_path, on_bad_lines='skip')
            print("Successfully loaded DASS by skipping bad lines.")
            print(f"Shape: {dass_data.shape}")
            print(f"Columns: {list(dass_data.columns[:10])}...")
        except Exception as e_skip:
            print(f"Failed to load by skipping bad lines: {e_skip}")

        # Let's also try to read just the first few lines to see the structure
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                print("\nFirst 5 lines of the file:")
                for i, line in enumerate(f):
                    if i < 5:
                        print(f"Line {i+1}: {repr(line)}")
                    else:
                        break
        except Exception as e_read:
            print(f"Failed to read file manually: {e_read}")

    except Exception as e:
        print(f"Unexpected error: {e}")


if __name__ == "__main__":
    debug_dass_loader()
