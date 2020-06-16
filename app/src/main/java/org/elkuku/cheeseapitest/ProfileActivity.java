package org.elkuku.cheeseapitest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    public static final String GOOGLE_ACCOUNT = "google_account";

    private GoogleSignInClient mGoogleSignInClient;

    private TextView profileName, txtDebug;

    private String apiToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mGoogleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);

        profileName = findViewById(R.id.profile_text);
        txtDebug = findViewById(R.id.profile_debug);

        Button signOut = findViewById(R.id.sign_out);
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                });
            }
        });

        Button cheeese = findViewById(R.id.btn_cheeese);
        cheeese.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtDebug.setText("querying...");
                requestCheeese();
            }
        });

        Button btnIngressEvents = findViewById(R.id.btn_events);
        btnIngressEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtDebug.setText("querying...");
                requestIngressEvents();
            }
        });

        setDataOnView();
    }

    private void requestIngressEvents() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://agents-4e.herokuapp.com/api/ingress_events.json?date_start[after]=2020-06-01";
        StringRequest getRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);

                        int id;
                        String name, type, link, result;
                        JSONArray array;
                        try {
                            array = new JSONArray(response);
                            result = "Ingress Events\n\n";
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject row = array.getJSONObject(i);
//                                Log.d("DEBUG", row.toString());
                                id = row.getInt("id");
                                name = row.getString("name");
                                type = row.getString("type");
                                link = row.getString("link");
                                result += name + " - " + link + "\n";
//                                Log.d("DATA", id + " - " + name);
                            }
                            txtDebug.setText(result);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("ERROR", "error => " + error.toString());
                        txtDebug.setText("error => " + error.getLocalizedMessage() + error.toString());
                    }
                }
        );
        queue.add(getRequest);
    }

    private void requestCheeese() {
        RequestQueue queue = Volley.newRequestQueue(this);
//        String url = "https://cheese-api-test.herokuapp.com/api/cheeses";
        String url = "https://cheese-api-test.herokuapp.com/api/users";
        StringRequest getRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                        txtDebug.setText(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        Log.d("ERROR", "error => " + error.toString());
                        txtDebug.setText("error => " + error.getLocalizedMessage() + error.toString());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("User-Agent", "Nintendo Gameboy");
                params.put("X-AUTH-TOKEN", apiToken);
                params.put("accept", "application/xml");
//                params.put("accept", "application/json");

                return params;
            }
        };
        queue.add(getRequest);
    }

    private void setDataOnView() {

        GoogleSignInAccount googleSignInAccount = getIntent().getParcelableExtra(GOOGLE_ACCOUNT);
        final String idToken = getIntent().getStringExtra("idToken");

        profileName.setText(googleSignInAccount.getDisplayName());

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://cheese-api-test.herokuapp.com/connect/google/api-token";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
//                        txtDebug.setText("Response: " + response);
                        JSONObject obj;
                        try {
                            Log.d("DEBUG", "Response: " + response);
                            obj = new JSONObject(response);
                            if (obj.has("error")) {
                                txtDebug.setText("Error: " + obj.getString("error"));
                            } else {
                                apiToken = obj.getString("token");
                                txtDebug.setText("Token is good!");
                            }
                        } catch (JSONException e) {
                            txtDebug.setText(e.getMessage());
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        txtDebug.setText("That didn't work!" + error.getMessage());
                    }
                }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<String, String>();
                MyData.put("idtoken", idToken);
                return MyData;
            }
        };

        queue.add(stringRequest);
    }
}