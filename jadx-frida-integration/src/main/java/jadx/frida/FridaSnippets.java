package jadx.frida;

import java.util.HashMap;
import java.util.Map;

/**
 * Predefined Frida snippets library
 */
public class FridaSnippets {

    public static final Map<String, String> SNIPPETS = new HashMap<>();

    static {
        // Bypass SSL Pinning
        SNIPPETS.put("Bypass SSL Pinning", "Java.perform(function() {\n" +
                "  console.log('[*] Bypassing SSL Pinning...');\n" +
                "  var TrustManagerImpl = Java.use('com.android.org.conscrypt.TrustManagerImpl');\n" +
                "  TrustManagerImpl.verifyChain.implementation = function(chain, authType, session, peer) {\n" +
                "    console.log('[+] Bypassed verifyChain');\n" +
                "  };\n" +
                "  var X509TrustManager = Java.use('javax.net.ssl.X509TrustManager');\n" +
                "  X509TrustManager.checkClientTrusted.implementation = function(chain, authType) {\n" +
                "    console.log('[+] Bypassed checkClientTrusted');\n" +
                "  };\n" +
                "  X509TrustManager.checkServerTrusted.implementation = function(chain, authType) {\n" +
                "    console.log('[+] Bypassed checkServerTrusted');\n" +
                "  };\n" +
                "});");

        // Bypass Root Detection
        SNIPPETS.put("Bypass Root Detection", "Java.perform(function() {\n" +
                "  console.log('[*] Bypassing Root Detection...');\n" +
                "  // Common root detection methods\n" +
                "  var File = Java.use('java.io.File');\n" +
                "  File.exists.implementation = function() {\n" +
                "    var path = this.getAbsolutePath();\n" +
                "    if (path.includes('su') || path.includes('Superuser') || path.includes('/system/xbin/')) {\n" +
                "      console.log('[+] Bypassed root check for: ' + path);\n" +
                "      return false;\n" +
                "    }\n" +
                "    return this.exists();\n" +
                "  };\n" +
                "  // Bypass Build.TAGS check\n" +
                "  var Build = Java.use('android.os.Build');\n" +
                "  var TagsField = Build.class.getDeclaredField('TAGS');\n" +
                "  TagsField.setAccessible(true);\n" +
                "  TagsField.set(null, 'release-keys');\n" +
                "});");

        // Log All Methods in a Class
        SNIPPETS.put("Log All Methods in Class", "Java.perform(function() {\n" +
                "  console.log('[*] Enter class name below:');\n" +
                "  // Replace \"com.example.TargetClass\" with your target\n" +
                "  var TargetClass = Java.use('com.example.TargetClass');\n" +
                "  var methods = TargetClass.class.getDeclaredMethods();\n" +
                "  methods.forEach(function(method) {\n" +
                "    var methodName = method.getName();\n" +
                "    console.log('[+] Hooking: ' + methodName);\n" +
                "    try {\n" +
                "      TargetClass[methodName].implementation = function() {\n" +
                "        var args = Array.from(arguments);\n" +
                "        console.log('[+] ' + methodName + ' called with args: ' + args.join(', '));\n" +
                "        var result = this[methodName].apply(this, arguments);\n" +
                "        console.log('[+] ' + methodName + ' returned: ' + result);\n" +
                "        return result;\n" +
                "      };\n" +
                "    } catch (e) {\n" +
                "      console.log('[-] Could not hook: ' + methodName);\n" +
                "    }\n" +
                "  });\n" +
                "});");

        // Dump SharedPreferences
        SNIPPETS.put("Dump SharedPreferences", "Java.perform(function() {\n" +
                "  console.log('[*] Dumping SharedPreferences...');\n" +
                "  var SharedPreferencesImpl = Java.use('android.app.SharedPreferencesImpl');\n" +
                "  SharedPreferencesImpl.getAll.implementation = function() {\n" +
                "    var map = this.getAll();\n" +
                "    console.log('[+] SharedPreferences dump:');\n" +
                "    var entries = map.entrySet().toArray();\n" +
                "    for (var i = 0; i < entries.length; i++) {\n" +
                "      console.log('    ' + entries[i].getKey() + ' = ' + entries[i].getValue());\n" +
                "    }\n" +
                "    return map;\n" +
                "  };\n" +
                "});");

        // Log HTTP Requests (OkHttp)
        SNIPPETS.put("Log OkHttp Requests", "Java.perform(function() {\n" +
                "  console.log('[*] Hooking OkHttp requests...');\n" +
                "  try {\n" +
                "    var RequestBuilder = Java.use('okhttp3.Request$Builder');\n" +
                "    RequestBuilder.build.implementation = function() {\n" +
                "      var request = this.build();\n" +
                "      console.log('[+] HTTP Request: ' + request.method() + ' ' + request.url());\n" +
                "      console.log('[+] Headers:');\n" +
                "      var headers = request.headers();\n" +
                "      for (var i = 0; i < headers.size(); i++) {\n" +
                "        console.log('    ' + headers.name(i) + ': ' + headers.value(i));\n" +
                "      }\n" +
                "      return request;\n" +
                "    };\n" +
                "  } catch (e) {\n" +
                "    console.log('[-] OkHttp not found');\n" +
                "  }\n" +
                "});");
    }
}
