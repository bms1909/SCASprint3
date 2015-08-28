package ulbra.bms.sca.controllers;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * x
 * Created by bms_c on 27/08/2015.
 */
public class CustomJsonArrayRequest extends Request<JSONArray> {
    private Response.Listener<JSONArray> response;
    private Map<String, String> params;


    public CustomJsonArrayRequest(int method, String url, Map<String, String> params, Response.Listener<JSONArray> response, Response.ErrorListener listener) {
        super(method, url, listener);
        this.params = params;
        this.response = response;
    }
    /*public CustomJsonArrayRequest(String url, Map<String, String> params, Response.Listener<JSONArray> response, Response.ErrorListener listener) {
        super(Method.GET, url, listener);
        this.params = params;
        this.response = response;
    }*/

    public Map<String, String> getParams() throws AuthFailureError {
        return params;
    }

    public Map<String, String> getHeaders() throws AuthFailureError {
        HashMap<String, String> header = new HashMap<String, String>();
        header.put("apiKey", "Essa e minha API KEY: json array");
        return (header);
    }

    public Priority getPriority() {
        return (Priority.NORMAL);
    }

    @Override
    protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
        try {
            String js = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            StringBuilder builder;
            //se nao comeca com [, eh um JSON object, e para garantir o reuso, eh transformado em jsonArray
            if (!js.startsWith("[")) {
                builder = new StringBuilder();
                builder.append("[");
                if (js.startsWith("t") || js.startsWith("f")) {
                    builder.append("{'resposta':");
                }
                builder.append(js);
                if (js.startsWith("t") || js.startsWith("f")) {
                    builder.append("}");
                }
                builder.append("]");
                js = builder.toString();
            }
            return (Response.success(new JSONArray(js), HttpHeaderParser.parseCacheHeaders(response)));
        } catch (UnsupportedEncodingException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    protected void deliverResponse(JSONArray response) {
        this.response.onResponse(response);
    }

}
