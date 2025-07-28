import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.json.*;

public class Shamir {

    static class Share {
        BigInteger x, y;

        Share(BigInteger x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
    }

    public static BigInteger lagrangeInterpolation(List<Share> shares) {
        BigInteger result = BigInteger.ZERO;

        for (int i = 0; i < shares.size(); i++) {
            BigInteger xi = shares.get(i).x;
            BigInteger yi = shares.get(i).y;
            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;

            for (int j = 0; j < shares.size(); j++) {
                if (i == j)
                    continue;
                BigInteger xj = shares.get(j).x;

                numerator = numerator.multiply(xj.negate());
                denominator = denominator.multiply(xi.subtract(xj));
            }

            BigInteger term = yi.multiply(numerator).divide(denominator);
            result = result.add(term);
        }

        return result;
    }

    public static List<Share> parseJSON(String filename) throws Exception {
        String content = new String(Files.readAllBytes(Paths.get(filename)));
        JSONObject json = new JSONObject(content);

        List<Share> shares = new ArrayList<>();

        for (String key : json.keySet()) {
            if (key.equals("keys"))
                continue;

            Object value = json.get(key);
            if (!(value instanceof JSONObject))
                continue;

            JSONObject obj = (JSONObject) value;

            if (!obj.has("base") || !obj.has("value"))
                continue;

            int base = Integer.parseInt(obj.getString("base"));
            String valueStr = obj.getString("value");

            BigInteger x = new BigInteger(key); // x = key
            BigInteger y = new BigInteger(valueStr, base); // y decoded from base

            shares.add(new Share(x, y));
        }

        return shares;
    }

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter JSON file name (e.g., testcase1.json): ");
        String filename = sc.nextLine().trim();

        List<Share> shares = parseJSON(filename);
        BigInteger secret = lagrangeInterpolation(shares);
        System.out.println("Recovered secret: " + secret);
    }
}
