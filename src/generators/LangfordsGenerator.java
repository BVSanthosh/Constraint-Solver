import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public final class LangfordsGenerator {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java LangfordsGenerator <k> <n>");
            System.out.println("for <k> sets of <n> integers");
            return;
        }
        int k = Integer.parseInt(args[0]);
        int n = Integer.parseInt(args[1]);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("langfords" + k + "_" + n + ".csp"))) {
            writer.write("//Langford, k = " + k + " n = " + n + "\n");

            int seqLength = k * n;
            writer.write("\n// Number of variables:\n" + seqLength + "\n");
            writer.write("\n// Domains of the variables: 1.. (inclusive)\n");
            for (int i = 0; i < seqLength; i++) {
                writer.write("1, " + seqLength + "\n");
            }

            writer.write("\n// constraints (vars indexed from 0, allowed tuples):\n");

            for (int block = 1; block <= n; block++) {
                for (int i = 0; i < k; i++) {
                    if (i < k - 1) {
                        writer.write("c(" + ((block - 1) * k + i) + ", " + ((block - 1) * k + i + 1) + ")\n");
                        for (int pos = 1; pos < seqLength; pos++) {
                            if (pos + block + 1 <= seqLength) {
                                writer.write(pos + ", " + (pos + block + 1) + "\n");
                            }
                        }
                        writer.write("\n");
                    }
                    for (int j = block * k; j < seqLength; j++) {
                        writer.write("c(" + ((block - 1) * k + i) + ", " + j + ")\n");
                        for (int val1 = 1; val1 <= seqLength; val1++) {
                            for (int val2 = 1; val2 <= seqLength; val2++) {
                                if (val1 != val2) {
                                    writer.write(val1 + ", " + val2 + "\n");
                                }
                            }
                        }
                        writer.write("\n");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
