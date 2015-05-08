package ulbra.bms.scaid5.controllers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import ulbra.bms.scaid5.R;
import ulbra.bms.scaid5.interfaces.usuarioCarregadoListener;
import ulbra.bms.scaid5.models.clsUsuarios;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends ActionBarActivity{

    private clsUsuarios mUsuario;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mUsuarioView;
    private EditText mPasswordView;
    private View mProgressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.txt_login_email);
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
                            if(which==DialogInterface.BUTTON_POSITIVE)
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
                if ( id == EditorInfo.IME_NULL) {
                    tentarLogin();
                    return true;
                }
                return false;
            }
        });

        mProgressView = findViewById(R.id.login_progress);
    }


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
            if(mUsuarioView.getHeight()>0)
            {
                mUsuario.nomeUsuario = mUsuarioView.getText().toString();
                mUsuario.emailUsuario= emailouUsuario;
                mUsuario.senhaUsuario=password;
                String retornoCadastro=mUsuario.cadastraUsuario();
                switch (retornoCadastro) {
                    case "SUCESSO":
                        mUsuario.carregaUsuario(emailouUsuario, password, this);
                        break;
                    case "JA_CADASTRADO":
                        mEmailView.setError("Usuário ou email já cadastrados!");
                        mUsuarioView.setError("Usuário ou email já cadastrados!");
                        mEmailView.requestFocus();
                        showProgress(false);
                        break;
                    case "ERRO_DOWNLOAD": {
                        AlertDialog.Builder novo = new AlertDialog.Builder(this);
                        novo.setMessage("Erro de conexão com a internet, confira sua internet e tente novamente");
                        novo.setPositiveButton("OK", null);
                        novo.show();
                        showProgress(false);
                        break;
                    }
                    default: {
                        AlertDialog.Builder novo = new AlertDialog.Builder(this);
                        novo.setMessage("Erro desconhecido, confira a estabilidade de sua internet e tente novamente, se persistir, contate o desenvolvedor e informe o código:\n" + retornoCadastro);
                        novo.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
                        novo.show();
                        showProgress(false);
                        break;
                    }
                }
            }
            else {
                mUsuario.carregaUsuario(emailouUsuario, password, this);
            }
        }
    }


    /**
     * Shows the progress UI and hides the login form.
     */
    public void showProgress(final boolean show) {

            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
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

    public void txtRecuperaSenha_Click(View view) {
        AlertDialog.Builder dlgRecupera = new AlertDialog.Builder(this);
        dlgRecupera.setTitle("Recuperar Conta");
        LayoutInflater inflater = this.getLayoutInflater();
        final View recuperaSenha = inflater.inflate(R.layout.layout_recupera_senha, null);
        //adiciona o layout recuperasenha como fonte para visual da view
        dlgRecupera.setView(recuperaSenha);
        dlgRecupera.setPositiveButton("Enviar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText nomeOuEmail = (EditText) recuperaSenha.findViewById(R.id.txt_recuperasenha);
                if (nomeOuEmail.getText().toString().equals("")) {
                    nomeOuEmail.setError("Campo obrigatório!");
                    nomeOuEmail.requestFocus();
                } else {
                    if(clsUsuarios.recuperaUsuario(nomeOuEmail.getText().toString())) {
                        Toast.makeText(LoginActivity.this, "Sua senha será enviada para o email cadastrado em breve", Toast.LENGTH_LONG).show();
                    }
                    else {
                        Toast.makeText(LoginActivity.this, "Não foi possível reconhecer seu email ou usuário, confira os dados informados", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        dlgRecupera.setCancelable(true);
        dlgRecupera.show();
    }

}



