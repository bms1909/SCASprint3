package ulbra.bms.scaid5.controllers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ulbra.bms.scaid5.R;
import ulbra.bms.scaid5.models.clsUsuarios;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends ActionBarActivity implements LoaderCallbacks<Cursor> {


    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mUsuarioView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.txt_login_email);
        popularAutoComplete();

        mUsuarioView = (EditText) findViewById(R.id.txt_login_usuario);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    tentarLogin();
                    return true;
                }
                return false;
            }
        });



        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void popularAutoComplete() {
        getLoaderManager().initLoader(0, null, this);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void tentarLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !(password.length() > 1)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!email.contains("@")) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);

            clsUsuarios login;
            if(!mUsuarioView.getText().toString().equals(""))
            {
                login = new clsUsuarios(mUsuarioView.getText().toString(),email,password);
                clsJSONget a;
                login.cadastraUsuario(LoginActivity.this);
            }

            login = clsUsuarios.carregaUsuario(email,password);

            showProgress(false);
            if (login.idUsuario > 0) {
                //todo login com sucesso
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            } else {
                mPasswordView.setError("Senha Incorreta");
                mPasswordView.requestFocus();
            }

          //  mAuthTask = new UserLoginTask(email, mUsuarioView.getText().toString(), password);
            //mAuthTask.execute((Void) null);
        }
    }


    /**
     * Shows the progress UI and hides the login form.
     */
    public void showProgress(final boolean show) {

            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    public void btnLogin_Click(View view) {
        tentarLogin();
    }

    public void cbxNovoUsuario_Click(View view) {
        LinearLayout habilitar = (LinearLayout) findViewById(R.id.ll_login);
        ViewGroup.LayoutParams lp = habilitar.getLayoutParams();
        AutoCompleteTextView txtNomeOuEmail = (AutoCompleteTextView) findViewById(R.id.txt_login_email);
        CheckBox cbxCadastra = (CheckBox) view;
        if(cbxCadastra.isChecked()) {
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            txtNomeOuEmail.setCompletionHint("Email");
        }
        else
        {
            mUsuarioView.setText("");
            lp.height = 0;
            txtNomeOuEmail.setCompletionHint("Email ou Usuário");
        }
        habilitar.setLayoutParams(lp);

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Integer> {

        private final String mEmail;
        private final String mUsuario;
        private final String mSenha;

        UserLoginTask(String email, String mUsuario, String password) {
            mEmail = email;
            this.mUsuario = mUsuario;
            mSenha = password;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            clsUsuarios login;
            if(!mUsuario.equals(""))
            {
                login = new clsUsuarios(mUsuario,mEmail,mSenha);
                login.cadastraUsuario(LoginActivity.this);
            }

            login = clsUsuarios.carregaUsuario(mEmail,mSenha);
            if(mEmail.equals("a@")&& mSenha.equals("aa"))
                return 1;

            return login.idUsuario;
        }

        @Override
        protected void onPostExecute( Integer success) {
            mAuthTask = null;
            showProgress(false);
            if (success > 0) {
                // login com sucesso

                SharedPreferences settings = getSharedPreferences("USUARIO", MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt("ID_USUARIO", success);
                editor.apply();

                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}



