package ServiceHandle;

import ServiceHandle.TrueChartService.Constants;
import ServiceHandle.TrueChartService.TCSessionResponse;
import com.google.gson.Gson;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TrueChartServiceHandle {

    private HttpServletResponse httpServletResponse;

    public TrueChartServiceHandle(){}

    public TrueChartServiceHandle(HttpServletResponse httpServletResponse)
    {
        this.httpServletResponse = httpServletResponse;
    }

    public TCSessionResponse getTCSession(String userName) throws IOException {

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        HttpPost httpPost = new HttpPost(Constants.SENSE_TC_SERVER_ENDPOINT);

//        httpPost.setHeader("content-type","");

        List<NameValuePair> form = new ArrayList<NameValuePair>();
        form.add(new BasicNameValuePair("action", Constants.SENSE_ACTION_GETSESSION));
        form.add(new BasicNameValuePair("model", "{\"documentHash\":\"78f31006ef296ba6f5775576cbfd99a464d4788a\"}"));
        form.add(new BasicNameValuePair("username", userName));

        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(form, Consts.UTF_8);
        httpPost.setEntity(entity);

        ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
            @Override
            public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity responseEntity = response.getEntity();
                    return responseEntity != null ? EntityUtils.toString(responseEntity) : null;
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            }
        };

        String responseBody = httpClient.execute(httpPost, responseHandler);

        Gson gson = new Gson();

        return  gson.fromJson(responseBody, TCSessionResponse.class);
    }
}

