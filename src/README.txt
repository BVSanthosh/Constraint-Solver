KEY INFORMATION:
- Solver.java: starting point to initiate the solver.
- FC.java: class for forward checking.
- MAC.java: class for maintaining arc consistency.
- csp files used for testing included in instances/ directory.
- generator files have been modified to write to a .csp file instead of printing to terminal.

HOW TO RUN:
1. Go to src directory.
2. Compile java code: make build.
3. Run java code: make run file=instances/<filename>.csp.
4. Program displays a prompt to choose between running FC or MAC.