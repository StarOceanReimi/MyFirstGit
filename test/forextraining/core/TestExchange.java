/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package forextraining.core;

import static forextraining.core.CurrencyExchange.EUR_USD;
import forextraining.core.order.MarketOrder;
import forextraining.tools.RC4CipherTools;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.junit.Test;

/**
 *
 * @author Reimi
 */
public class TestExchange {
    
    String rc4Script = "var key=\"aaf6cb4f0ced8a211c2728328597268509ade33040233a11af\";function hexEncode(e){var d=\"0123456789abcdef\",b=[],a=[],c;for(c=0;c<256;c++){b[c]=d.charAt(c>>4)+d.charAt(c&15)}for(c=0;c<e.length;c++){a[c]=b[e.charCodeAt(c)]}return a.join(\"\")}function hexDecode(f){var e=\"0123456789abcdef\",b=[],a=[],c=0,d;for(d=0;d<256;d++){b[e.charAt(d>>4)+e.charAt(d&15)]=String.fromCharCode(d)}if(!f.match(/^[a-f0-9]*$/i)){return false}if(f.length%2){f=\"0\"+f}for(d=0;d<f.length;d+=2){a[c++]=b[f.substr(d,2)]}return a.join(\"\")}function rc4(e,g){var b=0,d,a,h,f=[],c=[];for(d=0;d<256;d++){f[d]=d}for(d=0;d<256;d++){b=(b+f[d]+e.charCodeAt(d%e.length))%256;a=f[d];f[d]=f[b];f[b]=a}d=0;b=0;for(h=0;h<g.length;h++){d=(d+1)%256;b=(b+f[d])%256;a=f[d];f[d]=f[b];f[b]=a;c[c.length]=String.fromCharCode(g.charCodeAt(h)^f[(f[d]+f[b])%256])}return c.join(\"\")}function rc4decrypt(a){return rc4(key,hexDecode(a))};";
    
    @Test
    public void exchange() {

        System.out.println(EUR_USD.createBidMarketOrder(new BigDecimal("100000")));
        
    }
    
    @Test
    public void exechange1() throws IOException, ScriptException, NoSuchMethodException {
        
        String s = "c42054ed16cce59fd9039ef20200fb746dcabe47935c0000020555cd88ef7b885ba07fdf8acb6ed4bda2e67e21945c9a94ad81580cf9d4bded337b8b5aa6d3415f5afa3a3b3bb8ce06429e0e8ca36916db34764c52b14b6b71783c09932ce5b9e102528e987d6c85e85250727b4ab1c96a50a7ddecd0eac2d496983233656ae4e8a0c451376d3972afd37c88102cad955ba4c3c19b7ea3a27119e0e8e662cdeda18767d00fcdab36a306fecbfdc08a5277f8bbfaff53db5485e0c0a61e8dff6d1b96c30b6c3c55bbfd62d6fe8dbe6fe15484ddedb09d7b50a8fcca3a6c7d4a44717566fc674abe453e0eb9b27e793b05f6a379d6f3be154d36ec21d6328d0440a96a033ec0db43f0711ae74fece439eb3a245cefac11c901ff03ec33e685a1bb117bd49a63899673855f4ac34376da87d9cfeb7d72b46e7432c1bbe7f045b27206e93530e6fd16586694de17087f11f4ff35671cc891f92598a43ed131a290480a3a1dc6fcf415d86b3454c93d2612dcb7089e645749542ca3729b33ff6e2cd75601aad25c13c6f3f9f90967c3b76eacd625d121620d94bb7b47ef094408266868692d5608b56f75c9f5cdfe5fa80ad25f233baa9e1a9d7fa76d822caa19a4a1d4fe0845c0535dfd53ac61bab15840d0a4314147a467df8c724194d587e6ecf66a93c78837dab0130616a0b939174e74cd6f481a94da002e06ef9e53c8a90bce973d81d019f172fa940263c89d899c2ec723e3958c8a652fd1f8e21daa642dfdcdbac4fa00160261065ddc1cefa0c2493c05469c6d11e0b43063b85e69e80d6d885265a8b1ecfab7f81503a39b3ef141a983a0116305120204b118f7beda14a3a133680b32b7c248cd106e2d87882ae72f456b508792cc621440180b5887d3d3dda02794b5ac2601e04f8ab3e4c69de8890afcf944dae59bcf8424c6c7c1a7f5d56e723da133749ce9ebeaed4c205c538b40e082a9dd53252036443924f54801f1288526d80e30f2fdde0887f68b4b1ca6fda8cd0a901da1cd52dc0e1ce52271daf8370b83b7f8115b1a7fb34f74ce55e7a257ed19694bcde5c3358a37fcecbbbccff9d5f92b6ef73b2d8fc2e597428a6c4bdbd69ed2a5779d801aec26b96e18f923b7dd8abeee69d75b045b0c17e4fcddfc9c477fb61b51373e2dc59c3b2cbb6ac22b4f634fc220506b2fda1b721184ae6aefb1da2f3762f0bc4b29fe3498a7e12e8e6b4f5d87d96e4953c8907eadbb0db9c62a472e1af6848c5ce9c7cfa803d1fe7ea8ee10981b75baaf42160e3de773127b6a1570c26040dff97be1133d3f1bf31aa16a225e9503c2ff8b306eb0e3518993fdededad66802260e4be2c29b9bcbbb5655b78519026abaf7419c2122aa061cb02b56294d986816ba0e1ecb1612ac23c6e6ae3931dff6a989fa22175fd16428b2ec5571596d43d209f23a894ff700804884b0032e8e3c31d2078019f4649f7211f4a6c4b729b7833e0f35d06def15cc942b048aa3bbee40decfaa70cfad1847f79daedf46b21ece195557f2aa46152fc64553ecd96182594f9dd391a0b8db4b41a5fb5b8e24479201051a48b742c82436083d0f0a907b4f7fa33d288f5117b84185eb41971678157741b65ecc53bb68887340410fd915f79805d7ee2f65e914f42e9373bdd3b2515f34e83a73244382cd1bcc5d1bb0a094844d1be1abad866cf459636601acaec498677938c6763c73d7d65d7c4932caeeff48e37a943d5f25929047197f7fed2191f93a53f62738fd3d6708394c171816d24a997c69703babd5764323d058e4a0ed85da194f5d5e55bba4cc37be49660c5cc68d7088c7b51cd83ca7025853ac215d6eeee667f1b902a04ebbe7277529ab1739f5b10e3650114c0ea5147cddcde728f7813cf43969b721a32055691c1fdb44037c85313cefcf2178f018374494a0cd63fd60e7d492ffbcea32ece3f3c5c8ed3eb0a4e2daa4c92de90a98c15888a1f8d72541d373bd332be550b6e158b66f432af56d2fe1a95efd9b555853c749539dd2bf69053e9ccd7c9e7c1c0a6874efaa60f3fbb7c73c6c916a4e6b167eba0c9df1a80605fc564f56578ce9a3fbfe8f43b6cbf7091c04b4ba58f61901119bb95a2d61e81f35d3a16b832abe8f4a228f5d465bae5ec5971998fb76c7504ceef54c1f56b283e4b194dbf4ccb49a789c9a94aa659c41781bee06b8e16085d0221ee5776a96e6dafa224db00026c84a6153c016c022027c69e4e19d20ae8c26ad6604b48e8f0a45915ae684c531a007121fe3046ba9437568aca43989b20936588c864b6e127d1cb560f2102539bfdcd4f332964633c7ff57d81d5f1e6da98e18d31b02486837cce53fe8fdaca5d4fea4e6dc9e9aef0f070992991c53d3aea876e58c500482fabb638fc3bc7cda9dc5d684634f12ea087ae3be5b30533a2caca0d4398ad098909b478a13fe902d37201d99707869cf3a19fe9f6009cf9cbaeb44a7e70e4b808617cc20f394f8f0c03bdc66b5536b209e66ce6d99780f7dc30aa8c42a4dd31471044d6cdf35476d759f2ccb7ee03b7b4112461106b6ac6932ed4c151de3e909fffd027e032a9874d476a48da6812ee24d2a73b9158642563e8a457bec1073b7de2e840e672d220c588687d5076b1039ffb844468fb436ac9e7b980ded33c0a11202f9b3bf5db2eb17b2a5f4f743e3b9de93b75a3b80c255bea0af64b62d2d6407ca9f7e7786abde46d4c36b62a27003eb2124b806fb85f1bb43c6ae2945732f02aa8a42332c944c035f71ae9332aa7dd408b3ebb6456aba00b13c2f03a76251a4496f4f2b738e64dd0074f5cf2e3f20dd701bc358d294ed11bca20fe2f0766f94d01496cfe015b1a36a49e4f313f2236ddbcfabe08e50b9f9c0f237cc173749f91c183ff7ac51acbde767d9a6bd451195cb63e14a2c97544200f1ce9cd8cb6527cc85a638a2764d497303b55b3de35d2ac92a6c1e1e3562145e317a406c494c71ae38634e30291b06e80d67938796919c1509572202e6e5f931ec5275dc7ee85ca8a838ea8dc048ffac4ce5c1aabe7db51474c59b91ce069535bac180ed8020d9ad4d67c472e3ad7275c4f077ae90191e59ffdd540dd70930054cb721aa19cb365bf43c20be570ffbf248158778baf6f2d80ef8aafebc39621b0ad0b67d7d01d7852c3828ee67dfc36f9639275c91549804c7c163df991bdcadb99f2910b4c117c80f88df44242010e25ac8501bcbe6911e914320de095dd81ee07a8c5bcefa946a1d0b6896d7d58a40b20430b473f857d411608e32aac3888f18719b434cb66f5c1aaacf1f97ddf6f98ed397208d654d160c92f30bc10cb07943ceff5ab75357df9b81f3e49a182774effcbb01f3d06904e9a3dc3960b8d724010afbf4ee3541268ba9b05aeab2f4e0ac063d607b4064d0aad30b75a27eb1612dd6680dc922be02afc9e592b93f58c920011448b4be841e227840c5e18fb4519bfd3866975b2fd8d015b7447b41c1a7d9e65d6a0d1dc64a6c12087aeee5673874f27efdff3c88b9fdcbef0fc9e052a2230c5771fb0a2cb35b13230b8e5b3b4a0b51e3d9d508162fcf45c95ac8581cd8233dd9f5bb316eec8e79cdbf8114b07aa0ff82e77f21609b7e8424d3f80cc4e755179d5221a9abdfc3be8991d591aa914a207c459b1d5234ca584a1221bba9ff3dc4d8d00c5768bb6cacce01c5c313153e61ab23c01794295f6dc2c764dc1d4fb72baf1d67c6359f55d07b103bc6de5cd9842df3ac86f7913241c98f32ac0a97985756c061df9e83725f6837404e760a8fbca44cff6c2d7b4a9f89fa93e549699d38a0eff3765dd450c5cb0a6ecaa9adf124e9906071327fa555f9cbcd9ff6366e85fe75a42ae9d7cc6f1952539288f4874f025de811902b33430c14cde447c5df8fdce3a4ca07621a24fbbf67d2c288fcfde053e90fd1728646d4a984bdb8c42caa284f814bca3186d7bc15fc1e04c8dcb85c30060e1ca8e15f6d3be5b5ca98f4671a1d76a7880eb54c6d48585061aee20dd3e72c9eabf92c80398c3705566908fe2d031fce2b279482b2fc7cbdfb0f803be9717f565bc62b71ea05d2328f622994bc5e73d5878b987de651ca2c2dd1b9aa12627e5c4a3eab7c23b91d30c10381b52ce7c13c44d8da62b848de7b2f780b72a635954d524fd16eb27a36d6eb1d4705c2b643c4b78bd574eaa57bbf3917cdfaf4ab65b571b33fa24691b9d3695958e1ba45f778dff5948fe1272e78e7623c32de375a9aaf7b98eee111b5003ffa66279c50be0c96fabb6cfef290edec83196f6a4697d3533c9bf5017546dcd139bb2d7b7c392a9cd6eb11c166e5beb7da672ed91df0455270cb56100afded6730c9c2477bc7c177a78c3bfd91deec9a6364a45f949e111e1aad2867e0566fb3c7bc2e2cf5de3ced504672c27bb35472e1574d527461cdc835968df2850ffdb35020a6b74531d381f4227d910f096915a2af6adb9dca9363a1edb015c60e1c36c6d1ea41a687b434a7a85b76aaef2229e4839d87a84af40df80af503bd9fa723e9647a53797e9d83b8debba4b75bb753f2a352969561aef41cf628f168d694ad501a194c2e7d0529a0b9d18fc62af2133c9ed4288cb1d6394a6326b09bfb1877e5da356a7b7ec846886cf88261022920dd280748a6d66eff67dd3ac2e1d2c377ef989f962a46f492424fb2e86f9ac9be0cbed8fd7e1f570c827dd3d0bfd6ed9aa8a8e57d695baac5cf9044ef953a80c0d578c7e353d80ce42e5e47ca1170e6b60eeca3778f49c911b69481d436ad1f548c7ceb8045e44eef3c91daca71761977bfd8ddb150981677f28759a2532b0ef90208a4c0bc7671b74adafed9285ee82084799b8c99f131ab0c2f0f1d057ce6bea0baf04fc14ce0d14b499b5e32e3bbfd09f7d6f961ef8426c2a3498acd9b3c779a7e36cb20cd16255e515589a602d3890a6d2bd67d864cffc4f8c9ae10c6a9358b2482d017878d58f4742c9222807b3ee702a865f0efac68c64c0573aa7af343f22c582f0879962e9f90d520c6827f3d86d2e131cff4121b3c29e0df052a1f6fe237dc21e7c900465d4f9dc966825ffb3b1de3d4625fc768717fa5c03a239eb66be3118c24921774d3b14270dfd437ee86d0e91cf0387c87f211c7f1cc3e77100f1dfe5095b78b9a99f2991b9c94c9d531b9376c62c2e7e32041e3bf372492b80b5c56a7bf03515a7653a524b927909599ae0fcd3a3c0a7a2c3ad84bc127da71f3cfc61c4b8de0bcf275e9710a650c391f55036e24c6fc6a6727566b9208e58e1c70f99d2fb776566ad5bcdb27ea045323d5bde9760c955192d5778acc8a653bc681eac3982e54d7639ec1497c1a53d2e6d5d398d239a5d08a9955e1021cdf3b81d85d21e9ab2ee8f8ce2092a0fa657f0f49f73bbcc12ffafc530c5aae36dc143b68ced0a2ba4a8630e83845ac1e7f3d6e6df241de8c278c7a0d1e0e0330accc1806f92d2de3a92fb3149bf5197f11866d1afae30312e27f8354502bf29cd241789eadcac1316b9e3f24c247b5f323e6197761c69877c698914dd3474459fd68a19f150c38b828b9f3c1983e5d217d475ac1e7a62d3a259d088d48b2bdc825665fec7c91750694e1e24b2ec5993f58c2d1c3df3d968a87e48eb924dd3685061e5aa2dee0490a7241879234041ec6841d8f03ef47799d5f51bdc19ab63fd97f64b9bd300226a915ea3d78d466737fc7869163474be7e5e76d56f322f2900c01e06a02caedc4a12b21ad1a1a4c434052adad0d0c464969e16c88084c56717bd575b67e0d9d76276d5acec6bad1ae9b00cbd882fab1ecdd44b702f73cbe6e6bede39fcbef876ba69e34fbff6308193e1d000481087db7d77a578ab4dfb5ecb12aec1b2836151b5a34894d9a02b162f2c1037ab5f8479456ff00dddee456f4059f80b87ad922a20cd30ee92c67d0c8b793d3b2554bb1f";
        
        ScriptEngineManager sem = new ScriptEngineManager();
        ScriptEngine se = sem.getEngineByName("javascript");
        se.eval(rc4Script);
        Invocable invoker = (Invocable) se;
        Object obj = invoker.invokeFunction("rc4decrypt", s);
        Object encode = invoker.invokeFunction("hexEncode", obj);
        
    }
    
    volatile int counter;
    
    @Test
    public void threadGroupTest() throws InterruptedException {
        
        ThreadGroup tg = new ThreadGroup("tg");
        
        boolean[] stopSign = new boolean[1];
        stopSign[0] = false;
        for(int i=0; i<5; i++) {
            String name = "TG-Thread-"+i;
            Thread t = new Thread(tg, ()->{
                while(!stopSign[0]) {
                    counter += 1;
                    System.out.println("ThreadName:"+name+ ", Counter:"+(counter));
                    try {
                    Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                    }
                }
            }, name);
            t.start();
        }
        Thread.sleep(4000);
        
        Thread[] list = new Thread[tg.activeCount()];
        stopSign[0] = true;
        tg.enumerate(list);
        for (int i = 0; i < list.length; i++) {
            if(list[i] != null)
                list[i].join();
        }
        tg.destroy();
    }
    
    @Test
    public void tryGetData() throws InterruptedException, ScriptException, NoSuchMethodException {
        
        for(int i=0; i<15; i++) {
            try {
                String encriptResponse = getDataFromOnada();
                ScriptEngineManager sem = new ScriptEngineManager();
                ScriptEngine se = sem.getEngineByName("javascript");
                se.eval(rc4Script);
                Invocable invoker = (Invocable) se;
                Object obj = invoker.invokeFunction("rc4decrypt", encriptResponse);
                System.out.println(obj);
                PrintStream ps = new PrintStream("f:/getRate.txt");
                ps.println(obj);
            } catch(IOException ex) {
            }
            Thread.sleep(1000);
        }
    }
    
    public String getDataFromOnada() throws MalformedURLException, IOException {
        long tstamp = Clock.system(ZoneId.of("Etc/GMT-4")).instant().toEpochMilli();
        
        URL url = new URL("http://www.oanda.com/lfr/rates_lrrr?tstamp=" +  (tstamp-50) + "&lrrr_inverts=1");
        System.out.println(tstamp);
        System.out.println(Instant.ofEpochMilli(tstamp));
        URLConnection conn = url.openConnection();
        conn.addRequestProperty("Accept", "*/*");
        conn.addRequestProperty("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6");
        
        conn.addRequestProperty("Connection", "keep-alive");
        conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.154 Safari/537.36");
        conn.addRequestProperty("Referer", "http://www.oanda.com/currency/live-exchange-rates/");
        conn.addRequestProperty("Host", "www.oanda.com");
        
        conn.setDoOutput(true);
        conn.connect();
        
        InputStreamReader reader = new InputStreamReader(conn.getInputStream());
        char[] buffer = new char[1024];
        int len;
        StringWriter writer = new StringWriter();
        while((len = reader.read(buffer)) != -1) {
            writer.write(buffer, 0, len);
        }
        return writer.toString();
    }
}

























