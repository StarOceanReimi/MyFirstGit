/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package forextraining.tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Reimi
 */
public final class RC4CipherTools {
    
    private static final Logger LOG = LoggerFactory.getLogger(RC4CipherTools.class.getName());

    private static final int ITERATE_TIMES = 1000;
    
    private RC4CipherTools() {
        random = new SecureRandom(ByteBuffer.allocate(8).putLong(System.currentTimeMillis()).array());
        defaultKeyBytesMap = new ConcurrentHashMap<>();
    }
    
    private Charset charSet = Charset.defaultCharset();
    
    static RC4CipherTools instance;
    
    static SecureRandom random;
    
    private static final String DEFAULT_PASSWORD = RC4CipherTools.class.getName();
    private Map<String, byte[]> defaultKeyBytesMap;
    
    public static RC4CipherTools getInstance() {
        if(instance == null)
            instance = new RC4CipherTools();
        return instance;
    }
    
    public String encrypt(String password, String text) {

        InputStream is = new ByteArrayInputStream(text.getBytes(this.charSet));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ByteArrayOutputStream keyOut = new ByteArrayOutputStream();
        try {
            encrypt(password, keyOut, is, os);
        } catch (IOException ex) {
            LOG.error("error occurs during IO", ex);
        }
        String encrypt = bytesToHexString(os.toByteArray());
        defaultKeyBytesMap.put(encrypt, keyOut.toByteArray());
        return encrypt;
    }    
        
    public String encrypt(String text) {
        return encrypt(DEFAULT_PASSWORD, text);
    }
    
    public void encrypt(String password, OutputStream keyOut, InputStream input, OutputStream output) throws IOException {
        
        Objects.requireNonNull(password);
        Objects.requireNonNull(keyOut);
        Objects.requireNonNull(input);
        Objects.requireNonNull(output);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        createKey(password, baos);
        byte[] bytes = baos.toByteArray();
        keyOut.write(bytes);
        
        SecretKey key = loadKey(password, new ByteArrayInputStream(bytes));
        try {
            Cipher cipher = Cipher.getInstance("RC4");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            baos.reset();
            int len;
            while((len = input.read()) != -1) {
                baos.write(len);
            }
            byte[] encriptBytes = cipher.doFinal(baos.toByteArray());
            output.write(encriptBytes);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | 
                InvalidKeyException | 
                IllegalBlockSizeException | BadPaddingException ex) {
            LOG.error("errors occur when ciphering!", ex);
        }
    }
    
    public void encryptFile(String password, String keyFile, String inputFile, String outputFile) {
        try {
            FileOutputStream keyOut = new FileOutputStream(keyFile, false);
            FileInputStream in = new FileInputStream(inputFile);
            FileOutputStream out = new FileOutputStream(outputFile, false);
            encrypt(password, keyOut, in, out);
        } catch (FileNotFoundException ex) {
            LOG.error("Cant find file", ex);
        } catch (IOException ex) {
            LOG.error("IO Error", ex);
        }
    }
    
    public String decrypt(String text) {
        return decrypt(DEFAULT_PASSWORD, text);
    }
    
    public String decrypt(String password, String text) {
        byte[] keyBytes = defaultKeyBytesMap.get(text);
        InputStream keyIn = new ByteArrayInputStream(keyBytes);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream dataIn = new ByteArrayInputStream(hexStringToBytes(text));
        try {
            decrypt(password, keyIn, dataIn, output);
            defaultKeyBytesMap.remove(text);
        } catch (IOException ex) {
            LOG.error("error occurs during IO", ex);
        }
        return new String(output.toByteArray(), charSet);
    }    
    public void decrypt(String password, InputStream keyIn, InputStream input, OutputStream output) throws IOException {
        
        Objects.requireNonNull(password);
        Objects.requireNonNull(keyIn);
        Objects.requireNonNull(input);
        Objects.requireNonNull(output);
        
        try {
            SecretKey key = loadKey(password, keyIn);
            Cipher cipher = Cipher.getInstance("RC4");
            cipher.init(Cipher.DECRYPT_MODE, key);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int len;
            while((len = input.read()) != -1) {
                baos.write(len);
            }
            byte[] decryptBytes = cipher.doFinal(baos.toByteArray());
            output.write(decryptBytes);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | 
                 InvalidKeyException | IllegalBlockSizeException | 
                 BadPaddingException ex) {
            LOG.error("errors occur when ciphering!", ex);
        }
    }
    
    public void decryptFile(String password, String keyFile, String inputFile, String outputFile) {
        try {
            FileInputStream keyIn = new FileInputStream(keyFile);
            FileInputStream in = new FileInputStream(inputFile);
            FileOutputStream out = new FileOutputStream(outputFile, false);
            decrypt(password, keyIn, in, out);
        } catch (FileNotFoundException ex) {
            LOG.error("Cant find file", ex);
        } catch (IOException ex) {
            LOG.error("IO Error", ex);
        }
    }

    public void setCharSet(String charName) {
        this.charSet = Charset.forName(charName);
    }
    
    public void setCharSet(Charset charSet) {
        this.charSet = charSet;
    }

    private void createKey(String password, OutputStream os) throws IOException {

        Objects.requireNonNull(password);
        LOG.trace("creating key by password[{}]", password);
        
        try {

            KeyGenerator rc4KeyGen = KeyGenerator.getInstance("RC4");
            
            rc4KeyGen.init(256, random);
            SecretKey key = rc4KeyGen.generateKey();
            
            byte[] salt = new byte[8];
            random.nextBytes(salt);
            char[] passwordChars = password.toCharArray();
            PBEKeySpec pbeKeySpec = new PBEKeySpec(passwordChars);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
            SecretKey pbeKey = keyFactory.generateSecret(pbeKeySpec);
            PBEParameterSpec pbeParams = new PBEParameterSpec(salt, ITERATE_TIMES);
            Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
            cipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParams);
            byte[] encryptKeyBytes = cipher.doFinal(key.getEncoded());
            os.write(salt);
            os.write(encryptKeyBytes);
            
        } catch (NoSuchAlgorithmException ex) {
            LOG.error("NoSuchAlgorithm", ex);
        } catch (InvalidKeySpecException ex) {
            LOG.error("InvalidKeySpec", ex);
        } catch (NoSuchPaddingException ex) {
            LOG.error("NoSuchPadding", ex);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException ex) {
            LOG.error("Invalid", ex);
        } catch (IllegalBlockSizeException | BadPaddingException ex) {
            LOG.error("Illegal", ex);
        }
    }
    
    private SecretKey loadKey(String password, InputStream is) throws IOException {
        
        Objects.requireNonNull(password);
        Objects.requireNonNull(is);
        LOG.trace("loading key by password[{}]", password);
        
        byte[] salt = new byte[8];
        is.read(salt);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(is.available());
        int len;
        while((len = is.read()) != -1) {
            baos.write(len);
        }
        byte[] keyBytes = baos.toByteArray();
        
        PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray());
        PBEParameterSpec pbeParams = new PBEParameterSpec(salt, ITERATE_TIMES);
        SecretKeySpec keySpec = null;
        try {
            SecretKeyFactory pbeKeyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
            SecretKey pbeKey = pbeKeyFactory.generateSecret(pbeKeySpec);
            Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
            cipher.init(Cipher.DECRYPT_MODE, pbeKey, pbeParams);
            byte[] desciptKeyBytes = cipher.doFinal(keyBytes);
            keySpec = new SecretKeySpec(desciptKeyBytes, "RC4");

        } catch (NoSuchAlgorithmException ex) {
            LOG.error("NoSuchAlgorithm", ex);
        } catch (InvalidKeySpecException ex) {
            LOG.error("InvalidKeySpec", ex);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException ex) {
            LOG.error("Invalid", ex);
        } catch (IllegalBlockSizeException | BadPaddingException ex) {
            LOG.error("bad key! may be something wrong with your password or key bytes.");
        } catch (NoSuchPaddingException ex) {
            LOG.error("NoSuchPadding", ex);
        }
        
        return keySpec;
    }
    
    private static String bytesToHexString(byte[] bytes) {
        
        StringBuilder buffer = new StringBuilder(bytes.length);
        for (int i=0, n=bytes.length; i < n; i++) {
            byte b = bytes[i];
            buffer.append(byteToHexString(b));
        }
        return buffer.toString();
    }
    
    private static String byteToHexString(byte b) {
        int i = (int)b & 0xff;
        String chars =  "0123456789abcdef";
        byte high = (byte) (i >> 4);
        byte low  = (byte) (i - (high << 4));
        String highStr = String.valueOf(chars.charAt(high));
        String lowStr = String.valueOf(chars.charAt(low));
        return highStr + lowStr;
    }
    
    private static byte[] hexStringToBytes(String hexString) {
        Objects.requireNonNull(hexString);
        int len = hexString.length();
        if(len % 2 != 0) {
            throw new IllegalArgumentException("HexString length not even! size:" + hexString.length());
        }
        byte[] bytes = new byte[len/2];
        for(int i=0, n=0; i<len && n<(len/2); i+=2, n++) {
            String hex = String.valueOf(hexString.charAt(i)) +
                         String.valueOf(hexString.charAt(i+1));
            bytes[n] = hexStringToByte(hex);
        }
        return bytes;
    }
    
    private static byte hexStringToByte(String hexString) {
        Objects.requireNonNull(hexString);
        String chars =  "0123456789abcdef";
        int len = hexString.length();
        String supportStr = hexString.substring(len-2, len);
        int highStr = supportStr.charAt(0);
        int lowStr = supportStr.charAt(1);
        int high = chars.indexOf(highStr);
        int low = chars.indexOf(lowStr);
        high = high << 4;
        return (byte) (high + low);
    }
    
    private static void printUsageAndExit() {
        System.out.println("RC4 Encrypt Tool Usage: "
                + "-key <filePath> or -k "
                + "-input <filePath> or -i "
                + "-output <filePath> or -o "
                + "-encript or -e "
                + "-decript or -d "
                + "-password <password> or -p ");
        System.exit(0);
    }
    
    public static void main(String[] args) throws IOException {
        if(args.length == 0) {
            printUsageAndExit();
        }
        String keyPath = null;
        String inputPath = null;
        String outputPath = null;
        String encryptOrDecrypt = null;
        String password = null;
        for (int i = 0; i < args.length; i++) {
            String info = args[i];
            if(!info.startsWith("-")) {
                printUsageAndExit();
            }
            info = info.substring(1).toLowerCase();
            switch(info) {
                case "p":
                case "password":
                    password = args[++i];
                    break;
                case "k":
                case "key":
                    keyPath = args[++i];
                    break;
                case "i":
                case "input":
                    inputPath = args[++i];
                    break;
                case "o":
                case "output":
                    outputPath = args[++i];
                    break;
                case "e":
                case "encrypt":
                    encryptOrDecrypt = "e";
                    break;
                case "d":
                case "decrypt":
                    encryptOrDecrypt = "d";
                    break;
            }
        }
        
        Objects.requireNonNull(keyPath);
        Objects.requireNonNull(inputPath);
        Objects.requireNonNull(outputPath);
        Objects.requireNonNull(password);
        Objects.requireNonNull(encryptOrDecrypt);
        
        switch (encryptOrDecrypt) {
            case "e":
                getInstance().encryptFile(password, keyPath, inputPath, outputPath);
                break;
            case "d":
                getInstance().decryptFile(password, keyPath, inputPath, outputPath);
                break;
            default:
                printUsageAndExit();
                break;
        }
    }
    
}
