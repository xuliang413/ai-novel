import cn.hutool.crypto.symmetric.SM4;
import java.util.Base64;

public class Sm4EncryptTest {
    private static final String SM4_KEY = "1024lab__1024lab";

    private static String stringToHex(String input) {
        char[] chars = input.toCharArray();
        StringBuilder hex = new StringBuilder();
        for (char c : chars) {
            hex.append(Integer.toHexString((int) c));
        }
        return hex.toString();
    }

    private static byte[] hexToBytes(String hex) {
        int length = hex.length();
        byte[] result;
        if (length % 2 == 1) {
            length++;
            result = new byte[(length / 2)];
            hex = "0" + hex;
        } else {
            result = new byte[(length / 2)];
        }
        int j = 0;
        for (int i = 0; i < length; i += 2) {
            result[j] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
            j++;
        }
        return result;
    }

    public static void main(String[] args) {
        String data = args.length > 0 ? args[0] : "1024ok";
        SM4 sm4 = new SM4(hexToBytes(stringToHex(SM4_KEY)));
        String encryptHex = sm4.encryptHex(data);
        String base64 = Base64.getEncoder().encodeToString(encryptHex.getBytes());
        System.out.println("Encrypted: " + base64);
    }
}
