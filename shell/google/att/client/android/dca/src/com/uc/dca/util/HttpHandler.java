package com.uc.dca.util;

/*
<!--

        All Rights Reserved.

     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License.
-->
 */
/*
<!--
$Id::                                                                           $: Id of last commit
$Rev::                                                                          $: Revision of last commit
$Author::                                                                       $: Author of last commit
$Date::                                                                         $: Date of last commit
-->
*/

import android.util.Log;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.util.List;

public class HttpHandler {

    public static final String TAG = "HttpHandler";

    public HttpHandler() {
        super();
    }

    public String get(String request)
            throws IOException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(request);
        Log.d(TAG, "executing request " + httpGet.getURI());
        // Create a response handler
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String responseBody = httpClient.execute(httpGet, responseHandler);
//        Log.d(TAG, responseBody);
        return responseBody;
    }

    public String post(String uri, List<NameValuePair> nvps) throws IOException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(uri);
        httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
        Log.d(TAG, "executing post " + httpPost.getURI());
        // Create a response handler
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        String responseBody = httpClient.execute(httpPost, responseHandler);
//        Log.d(TAG, responseBody);
        return responseBody;

    }
}
