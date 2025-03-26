package org.webflux.helper;

import org.assertj.core.api.WithAssertions;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class SecurityUtilsTest implements WithAssertions {


    @Test
    public void givenString_whenEncrypt_thenSuccess()
            throws NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException,
            BadPaddingException, InvalidAlgorithmParameterException, NoSuchPaddingException {

        String input = "baeldung";
        SecretKey key = AESUtils.generateKey(128);
        IvParameterSpec ivParameterSpec = AESUtils.generateIv();
        String algorithm = "AES/CBC/PKCS5Padding";
        String cipherText = AESUtils.encrypt(algorithm, input, key, ivParameterSpec);
        String plainText = AESUtils.decrypt(algorithm, cipherText, key, ivParameterSpec);
        Assertions.assertEquals(input, plainText);
    }

    @Test
    public void givenFile_whenEncrypt_thenSuccess()
            throws NoSuchAlgorithmException, IOException, IllegalBlockSizeException,
            InvalidKeyException, BadPaddingException, InvalidAlgorithmParameterException,
            NoSuchPaddingException {

        SecretKey key = AESUtils.generateKey(128);
        String algorithm = "AES/CBC/PKCS5Padding";
        IvParameterSpec ivParameterSpec = AESUtils.generateIv();
        Resource resource = new ClassPathResource("inputFile/baeldung.txt");
        File inputFile = resource.getFile();
        File encryptedFile = new File("classpath:baeldung.encrypted");
        File decryptedFile = new File("document.decrypted");
        AESUtils.encryptFile(algorithm, key, ivParameterSpec, inputFile, encryptedFile);
        AESUtils.decryptFile(
                algorithm, key, ivParameterSpec, encryptedFile, decryptedFile);
        assertThat(inputFile).hasSameTextualContentAs(decryptedFile);
    }
}
