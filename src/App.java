import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class App {
  private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

  public static String bytesToHex(byte[] bytes) {
    char[] hexChars = new char[bytes.length * 2];
    for (int j = 0; j < bytes.length; j++) {
      int v = bytes[j] & 0xFF;
      hexChars[j * 2] = HEX_ARRAY[v >>> 4];
      hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
    }
    return new String(hexChars);
  }

  public static String getHmacSha256(byte[] secretKey, String value) {
    Mac sha256Hmac;
    String result = "";

    try {
      sha256Hmac = Mac.getInstance("HmacSHA256");
      SecretKeySpec keySpec = new SecretKeySpec(secretKey, "HmacSHA256");
      sha256Hmac.init(keySpec);
      byte[] macData = sha256Hmac.doFinal(value.getBytes(StandardCharsets.UTF_8));

      result = bytesToHex(macData);
    } catch (InvalidKeyException | NoSuchAlgorithmException e) {
      e.printStackTrace();
    }

    return result;
  }

  public static void printPossibleVariants(String[] variants) {
    System.out.println("Available moves:");
    for (int i = 0; i < variants.length; i++) {
      System.out.println((i + 1) + ": " + variants[i]);
    }
    System.out.println("0: exit");
  }

  public static Integer calculateResult(String[] variants, Integer userTurn, Integer pcTurn) {
    Integer halfLength = variants.length / 2;
    if (pcTurn == userTurn) {
      return 0;
    }

    if (pcTurn > userTurn) {
      Integer diff = pcTurn - userTurn;

      if (diff > halfLength) {
        return 1;
      }

      return -1;
    }

    if (pcTurn < userTurn) {
      Integer diff = userTurn - pcTurn;

      if (diff > halfLength) {
        return -1;
      }

      return 1;
    }

    return 0;
  }

  public static void logResult(Integer gameCalculation) {
    if (gameCalculation == 0) {
      System.out.println("Draw!");
    }
    if (gameCalculation == 1) {
      System.out.println("You win!");
    }
    if (gameCalculation == -1) {
      System.out.println("You lose!");
    }
  }

  public static boolean checkUserInput(String userInput, Integer arrayLength, Scanner scanner) {
    try {
      Integer input = Integer.parseInt(userInput);

      if (input > arrayLength) {
        throw new NumberFormatException();
      }

      return false;
    } catch (NumberFormatException e) {
      System.out.println("Wrong input! Should be integer from 0 to " + arrayLength + ". You entered: " + userInput);

      return true;
    }
  }

  public static boolean checkForDuplicates(String[] array) {
    HashSet<String> set = new HashSet<String>();
    for (int i = 0; i < array.length; i++) {
      set.add(array[i]);
    }

    return array.length != set.size();
  }

  public static void logTurn(String[] array, String userInput, Integer pcTurn) {
    System.out.println("Your move: " + array[Integer.parseInt(userInput) - 1]);
    System.out.println("Computer move: " + array[pcTurn]);
    logResult(calculateResult(array, Integer.parseInt(userInput) - 1, pcTurn));
  }

  public static void main(String[] args) throws Exception {
    SecureRandom ranGen = new SecureRandom();
    byte[] aesKey = new byte[16];
    Scanner scanner = new Scanner(System.in);
    Random random = new Random();

    ranGen.nextBytes(aesKey);

    if (args.length < 3) {
      System.out.println("Wrong input! Elements should be more then 2");
      scanner.close();
      return;
    }

    if (checkForDuplicates(args)) {
      System.out.println("Wrong input! Elements should be unique");
      scanner.close();
      return;
    }

    if (args.length % 2 == 0 ) {
      System.out.println("Wrong input! There must be an odd number of items");
      scanner.close();
      return;
    }

    Integer pcTurn = random.nextInt(args.length);
    System.out.println("HMAC: " + getHmacSha256(aesKey, args[pcTurn]));

    printPossibleVariants(args);

    System.out.print("Enter your move: ");
    String userInput = scanner.nextLine();

    while (checkUserInput(userInput, args.length, scanner)) {
      System.out.print("Enter your move: ");
      userInput = scanner.nextLine();
    }

    if (userInput.equals("0")) {
      scanner.close();
      System.out.println(bytesToHex(aesKey));
      return;
    }

    logTurn(args, userInput, pcTurn);

    while (true) {
      pcTurn = random.nextInt(args.length);
      System.out.println("HMAC: " + getHmacSha256(aesKey, args[pcTurn]));

      printPossibleVariants(args);

      System.out.print("Enter your move: ");
      userInput = scanner.nextLine();

      while (checkUserInput(userInput, args.length, scanner)) {
        System.out.print("Enter your move: ");
        userInput = scanner.nextLine();
      }

      if (userInput.equals("0")) {
        break;
      }

      logTurn(args, userInput, pcTurn);
    }
    scanner.close();
    System.out.println(bytesToHex(aesKey));
  }
}
