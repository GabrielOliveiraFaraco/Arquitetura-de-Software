package crawler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PrecoParser {

    private static final Pattern BRL = Pattern.compile(
            "R\\$\\s*([\\d]{1,3}(?:\\.\\d{3})*|\\d+)(?:,([\\d]{2}))?",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern NUMERO_JSON = Pattern.compile(
            "\"(?:price|lowPrice|highPrice|amount|salePrice)\"\\s*:\\s*\"?([\\d]+(?:[.,][\\d]+)?)\"?",
            Pattern.CASE_INSENSITIVE);

    private PrecoParser() {
    }

    public static Optional<Float> parseTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            return Optional.empty();
        }

        List<Float> candidatos = new ArrayList<>();
        Matcher matcher = BRL.matcher(texto);
        while (matcher.find()) {
            parsePartes(matcher.group(1), matcher.group(2)).ifPresent(candidatos::add);
        }

        Matcher jsonMatcher = NUMERO_JSON.matcher(texto);
        while (jsonMatcher.find()) {
            parseNumeroSimples(jsonMatcher.group(1)).ifPresent(candidatos::add);
        }

        return candidatos.stream()
                .filter(PrecoParser::precoPlausivel)
                .findFirst();
    }

    public static Optional<Float> parseNumero(String valor) {
        if (valor == null || valor.isBlank()) {
            return Optional.empty();
        }
        String limpo = valor.trim().replace("R$", "").trim();
        if (limpo.contains(",")) {
            return parseTexto("R$ " + limpo);
        }
        return parseNumeroSimples(limpo);
    }

    static boolean precoPlausivel(float valor) {
        return valor > 10f && valor < 1_000_000f;
    }

    private static Optional<Float> parsePartes(String inteiro, String centavos) {
        if (inteiro == null) {
            return Optional.empty();
        }
        String normalizado = inteiro.replace(".", "");
        try {
            float valor = Float.parseFloat(normalizado);
            if (centavos != null && !centavos.isBlank()) {
                valor += Float.parseFloat(centavos) / 100f;
            }
            return Optional.of(valor);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private static Optional<Float> parseNumeroSimples(String numero) {
        if (numero == null) {
            return Optional.empty();
        }
        String valor = numero.replace(",", ".");
        try {
            return Optional.of(Float.parseFloat(valor));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
