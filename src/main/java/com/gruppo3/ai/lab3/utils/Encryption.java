package com.gruppo3.ai.lab3.utils;

import com.gruppo3.ai.lab3.model.PositionEntity;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Encryption {

    private static final Random RANDOM = new SecureRandom();
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;

    //mi ritorna un sale
    private static byte[] getSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return salt;
    }

    public static byte[] getHashedId(String id){
        PBEKeySpec spec = new PBEKeySpec(id.toCharArray(), getSalt(), ITERATIONS, KEY_LENGTH);
        Arrays.fill(id.toCharArray(), Character.MIN_VALUE);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new AssertionError("Error while hashing a password: " + e.getMessage(), e);
        } finally {
            spec.clearPassword();
        }
    }

    public static List<PositionEntity> cryptId(List<String> listNoUserDuplication, List<PositionEntity> list) {
        List<PositionEntity> cryptList = new ArrayList<>();
        for (int j = 0; j < listNoUserDuplication.size(); j++) {
            String hashed_id = Encryption.getHashedId(listNoUserDuplication.get(j)).toString();
            for (PositionEntity pos : list) {
                if (pos.getSubject()
                        .equals(listNoUserDuplication
                                .get(j))) {
                    pos.setId(hashed_id);
                    cryptList.add(pos);
                }
            }
        }
        return cryptList;
    }
}
