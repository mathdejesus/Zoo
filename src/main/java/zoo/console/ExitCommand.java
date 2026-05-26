package zoo.console;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Recognizes exit commands in several languages (case-insensitive).
 * <p>
 * Uses {@link Set#copyOf} to create an immutable set after construction,
 * demonstrating the Java 10+ immutable-collection pattern.
 * Input normalization (trim + lowercase) ensures robust matching.
 */
public final class ExitCommand {

    private static final Set<String> EXIT_WORDS = buildExitWords();

    private static Set<String> buildExitWords() {
        Set<String> words = new HashSet<>();
        String[] defaults = {
                "exit", "sair", "quit", "end", "ende", "fine", "salir", "terminar",
                "parar", "stop", "bye", "tchau", "chau", "adeus", "arrivederci",
                "ciao", "tschuss", "tschüss", "beenden", "afslut", "sluit", "avslutt",
                "avsluta", "lopeta", "zakoncz", "zakończ", "konec", "koniec", "finito",
                "fin", "finalizar", "encerrar", "fechar", "cerrar", "schließen", "schliessen"
        };
        for (String word : defaults) {
            words.add(word);
        }
        return Set.copyOf(words);
    }

    private ExitCommand() {
    }

    /**
     * Checks if the input matches a known exit word in any supported language.
     * Input is normalized (trimmed, lower-cased) before matching.
     */
    public static boolean isExit(String input) {
        if (input == null || input.isBlank()) {
            return false;
        }
        String normalized = input.trim().toLowerCase(Locale.ROOT);
        return EXIT_WORDS.contains(normalized);
    }

    /**
     * Checks if the input is the integer zero.
     */
    public static boolean isZero(String input) {
        if (input == null || input.isBlank()) {
            return false;
        }
        try {
            return Integer.parseInt(input.trim()) == 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static String supportedExitHint() {
        return String.join(", ", EXIT_WORDS);
    }
}
