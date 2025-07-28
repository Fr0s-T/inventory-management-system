package Tests;

import Utilities.HashingUtility;
/*
 *
 * Author: @Frost
 *
 */
public class TestHashing {

    public static void main(String[] args){
        HashingUtility hashingUtility = new HashingUtility();
        System.out.println("123 as a String would be hashed into: "+hashingUtility.md5Hash("123"));
    }
}
