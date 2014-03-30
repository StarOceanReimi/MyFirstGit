/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package forextraining.core;

import forextraining.tools.RC4CipherTools;
import java.io.IOException;
import org.junit.Test;

/**
 *
 * @author Reimi
 */
public class TestCipherTool {
    
    @Test
    public void testCipherTool() throws IOException {
    
        RC4CipherTools.main(new String[]{
            "-e",
            "-o",
            "e:/output.txt",
            "-i",
            "e:/设计构思.txt",
            "-k",
            "e:/output_key",
            "-p",
            "860226"
        });
    }
    
    @Test
    public void testCipherTool1() throws IOException {
        RC4CipherTools.main(new String[]{
            "-d",
            "-o",
            "e:/设计构思解密.txt",
            "-i",
            "e:/output.txt",
            "-k",
            "e:/output_key",
            "-p",
            "860226"
        });
    }
    
}
