package ulbra.bms.scaid5.controllers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ulbra.bms.scaid5.R;
import ulbra.bms.scaid5.interfaces.usuarioCarregadoListener;
import ulbra.bms.scaid5.models.clsUsuarios;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends ActionBarActivity implements LoaderCallbacks<Cursor> {

    private clsUsuarios mUsuario;

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
        mUsuario= new clsUsuarios();
        mUsuario.addListener(new usuarioCarregadoListener() {
            @Override
            public void usuarioCarregado(clsUsuarios Usuario) {
                //login com sucesso
                if (Usuario.idUsuario > 0) {
                    SharedPreferences settings = getSharedPreferences("USUARIO", 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putInt("ID_USUARIO", Usuario.idUsuario);
                    editor.apply();

                    startActivity(new Intent(LoginActivity.this, MainActivity.class));

                } else if(Usuario.senhaUsuario.equals("INCORRETA")){
                    //senha incorreta
                    mPasswordView.setError("Senha Incorreta");
                    mPasswordView.requestFocus();
                }
                else if(Usuario.nomeUsuario.equals("INVALIDO"))
                {
                    mEmailView.setError("Usuário ou senha incorretos");
                }
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setTitle("Erro desconhecido");
                    builder.setMessage("Não foi possível processar o seu login, por favor, confira a estabilidade de sua conexão com a internet e tente novamente, se o erro persistir, contate o desenvolvedor");
                    //se o malandro pressionar fora do AlertDialog, fecha o aplicativo
                    builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            finish();
                        }
                    });

                    builder.setPositiveButton("Repetir", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(which==1)
                            {
                                tentarLogin();
                            }
                            else {
                                finish();
                            }
                        }
                    }).setNegativeButton("Fechar", null);
                    builder.create().show();
                }
                showProgress(false);
            }
        });
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

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String emailouUsuario = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.TODO alterar para 4
        if (!TextUtils.isEmpty(password) && !(password.length() > 1)) {
            mPasswordView.setError("Senha inválida, deve ser maior que 4 dígitos");
            focusView = mPasswordView;
            cancel = true;
        }


        if (TextUtils.isEmpty(emailouUsuario)) {
            mEmailView.setError("Campo obrigatório!");
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
            if(!mUsuarioView.getText().toString().equals(""))
            {
                mUsuario.nomeUsuario = mUsuarioView.getText().toString();
                mUsuario.emailUsuario= emailouUsuario;
                mUsuario.senhaUsuario=password;
                mUsuario.cadastraUsuario(LoginActivity.this);
            }

            mUsuario.carregaUsuario(emailouUsuario,password,this);


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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.layout_tipos_alerta, emails);
        mEmailView.setAdapter(adapter);
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
            txtNomeOuEmail.setHint("Email");
        }
        else
        {
            mUsuarioView.setText("");
            lp.height = 0;
            txtNomeOuEmail.setHint("Email ou Usuário");
        }
        habilitar.setLayoutParams(lp);

    }

    public void btnRecuperaSenha_click(View view) {
        /*mPasswordView.setHeight(0);
        mUsuarioView.setHeight(0);
        mEmailView.setHint("Informe seu email ");*/
    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

}



