"""
Corrected debug script to find exact file and folder names.
"""

import os
from pathlib import Path


def check_data_paths():
    """
    Check what files and folders actually exist in the data directory.
    """
    # Since we're running from project root, data folder should be right here
    data_path = Path("data")  # Just 'data' folder in current directory
    raw_path = data_path / "raw"

    print(f"Current working directory: {Path.cwd()}")
    print(f"Checking data path: {data_path.absolute()}")
    print(f"Data path exists: {data_path.exists()}")
    print(f"Raw path: {raw_path.absolute()}")
    print(f"Raw path exists: {raw_path.exists()}")

    if raw_path.exists():
        print("\n" + "="*60)
        print("DETAILED LISTING OF RAW DIRECTORY:")
        print("="*60)

        # List all items in the raw directory with exact names
        items = list(raw_path.iterdir())
        print(f"Total items found: {len(items)}")

        for i, item in enumerate(items, 1):
            if item.is_file():
                print(f"{i}. FILE: '{item.name}'")
                print(f"   Full path: {item}")
                print(f"   Size: {item.stat().st_size} bytes")
            elif item.is_dir():
                print(f"{i}. FOLDER: '{item.name}'")
                print(f"   Full path: {item}")

                # List contents of each folder
                try:
                    sub_items = list(item.iterdir())
                    print(f"   Contains {len(sub_items)} items:")
                    for j, sub_item in enumerate(sub_items, 1):
                        if sub_item.is_file():
                            print(f"     {j}. FILE: '{sub_item.name}'")
                            print(
                                f"        Size: {sub_item.stat().st_size} bytes")
                        else:
                            print(f"     {j}. FOLDER: '{sub_item.name}'")
                except PermissionError:
                    print(f"   (Cannot access contents - permission denied)")
                except Exception as e:
                    print(f"   (Error accessing contents: {e})")
            print()  # Empty line for readability

        # Now let's specifically look for our target files
        print("\n" + "="*60)
        print("SEARCHING FOR TARGET FILES:")
        print("="*60)

        # Search for DASS-related files
        print("1. Searching for DASS dataset...")
        dass_found = False
        for item in raw_path.rglob("*"):  # Search recursively
            if "depression" in item.name.lower() and "anxiety" in item.name.lower() and item.suffix == ".csv":
                print(f"   FOUND DASS FILE: '{item.name}'")
                print(f"   Location: {item.parent}")
                print(f"   Full path: {item}")
                dass_found = True
        if not dass_found:
            print("   No DASS CSV file found")

        # Search for Tech Survey files
        print("\n2. Searching for Tech Survey 2014...")
        tech_found = False
        for item in raw_path.rglob("*.csv"):
            if "mental health" in item.name.lower() and "tech" in item.name.lower() and "2014" in item.name.lower():
                print(f"   FOUND TECH SURVEY: '{item.name}'")
                print(f"   Location: {item.parent}")
                print(f"   Full path: {item}")
                tech_found = True
        if not tech_found:
            print("   No Tech Survey 2014 file found")

        # Search for Sleep Health files
        print("\n3. Searching for Sleep Health dataset...")
        sleep_found = False
        for item in raw_path.rglob("*.csv"):
            if "sleep" in item.name.lower():
                print(f"   FOUND SLEEP FILE: '{item.name}'")
                print(f"   Location: {item.parent}")
                print(f"   Full path: {item}")
                sleep_found = True
        if not sleep_found:
            print("   No Sleep Health file found")

    else:
        print("Raw data directory not found!")

        # Let's check what's in the current directory
        print("\nContents of current directory (project root):")
        current_dir = Path(".")
        for item in current_dir.iterdir():
            print(f"  {item.name} ({'folder' if item.is_dir() else 'file'})")


if __name__ == "__main__":
    check_data_paths()
