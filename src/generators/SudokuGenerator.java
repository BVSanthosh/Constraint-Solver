import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public final class SudokuGenerator {

    private static void diseqTuples(BufferedWriter writer) throws IOException {
        for (int val1 = 1; val1 <= 9; val1++)
            for (int val2 = 1; val2 <= 9; val2++)
                if (val1 != val2)
                    writer.write(val1 + ", " + val2 + "\n");
    }

    public static void main(String[] args) {
        if (args.length != 0) {
            System.out.println("Usage: java SudokuGenerator");
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("Sudoku.csp"))) {
            writer.write("//Sudoku.\n");
            writer.write("\n// Always 81 variables:\n" + 81 + "\n");
            writer.write("\n// Domains of the variables: 1..9 (inclusive)\n");
            writer.write("\n// Edit the following to provide clues\n");
            for (int i = 0; i < 81; i++)
                writer.write("1, 9\n");
            writer.write("\n// constraints (vars indexed from 0, allowed tuples):\n");

            // Rows
            for (int row = 1; row <= 9; row++) {
                writer.write("//Row: " + row + "\n");
                for (int col1 = 1; col1 <= 8; col1++)
                    for (int col2 = col1 + 1; col2 <= 9; col2++) {
                        writer.write("c(" + ((row - 1) * 9 + col1 - 1) + ", " + ((row - 1) * 9 + col2 - 1) + ")\n");
                        diseqTuples(writer);
                        writer.write("\n");
                    }
            }

            // Cols
            for (int col = 1; col <= 9; col++) {
                writer.write("//Col: " + col + "\n");
                for (int row1 = 1; row1 <= 8; row1++)
                    for (int row2 = row1 + 1; row2 <= 9; row2++) {
                        writer.write("c(" + ((row1 - 1) * 9 + col - 1) + ", " + ((row2 - 1) * 9 + col - 1) + ")\n");
                        diseqTuples(writer);
                        writer.write("\n");
                    }
            }

            // 3 x 3 subsquares
            for (int subRow = 1; subRow <= 7; subRow += 3)
                for (int subCol = 1; subCol <= 7; subCol += 3) {
                    writer.write("//Subsquare starting at row: " + subRow + ", col: " + subCol + "\n");
                    for (int row1 = subRow; row1 <= subRow + 2; row1++)
                        for (int col1 = subCol; col1 <= subCol + 2; col1++)
                            for (int row2 = row1; row2 <= subRow + 2; row2++)
                                for (int col2 = subCol; col2 <= subCol + 2; col2++) {
                                    if ((row2 > row1) || (col2 > col1)) {
                                        writer.write("c(" + ((row1 - 1) * 9 + col1 - 1) + ", " + ((row2 - 1) * 9 + col2 - 1) + ")\n");
                                        diseqTuples(writer);
                                        writer.write("\n");
                                    }
                                }
                }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
