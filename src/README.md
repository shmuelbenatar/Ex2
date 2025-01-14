
---

# Ex2 Spreadsheet Simulator

## Overview
This Java project implements a basic spreadsheet simulator as part of the "Introduction to Computer Science" course. It utilizes object-oriented programming to create a functional spreadsheet that can handle text, numbers, and formulas.

## Features
- **Cell Types:** Supports text, numerical values, and formulas.
- **Formula Evaluation:** Robust parsing and evaluation of formulas including support for circular dependency detection.
- **GUI Implementation:** Visual representation of the spreadsheet using `StdDrawEx2` with interactive cell editing.
- **Error Handling:** Handles various error scenarios gracefully including invalid formulas and circular references.
- **Testing:** Includes a comprehensive suite of unit tests using JUnit to ensure functionality.

## Project Structure
- `Cell.java`: Interface for spreadsheet entries.
- `Index2D.java`: Interface for managing cell indices.
- `Sheet.java`: Interface for the spreadsheet.
- `CellEntry.java`: Implements `Index2D`, handles cell referencing.
- `SCell.java`: Implements `Cell`, defines cell behavior.
- `Ex2Sheet.java`: Implements `Sheet`, main logic for spreadsheet operations.
- `Ex2GUI.java`: Graphical user interface for the spreadsheet.
- `StdDrawEx2.java`: Utility class for drawing the GUI components.
- `Ex2Tests.java`: JUnit tests for validating cell operations and formula evaluations.

## GUI Example
Below is an example of the graphical user interface displaying the spreadsheet:

![Spreadsheet Example](![img.png](img.png))



---
