package ulbra.bms.sca.controllers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
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

import ulbra.bms.sca.R;
import ulbra.bms.sca.interfaces.booleanRetornadoListener;
import ulbra.bms.sca.interfaces.usuarioCarregadoListener;
import ulbra.bms.sca.models.clsCategorias;
import ulbra.bms.sca.models.clsUsuarios;


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
        //necessario pois o titulo da activity virava o titulo do app
        ActionBar ab = getSupportActionBar();
        ab.setTitle(getResources().getString(R.string.title_activity_login));
        //sincroniza categorias com o servidor antes do login, tarefa assincrona, nao interfere nas demais


        clsCategorias.sincronizaCategoriasServidor(LoginActivity.this);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.txt_login_email);
        mUsuarioView = (EditText) findViewById(R.id.txt_login_usuario);
        mPasswordView = (EditText) findViewById(R.id.txt_login_senha);
        mEmailView.setNextFocusForwardId(R.id.txt_login_senha);

        mUsuario= new clsUsuarios();
        mUsuario.addListener(new usuarioCarregadoListener() {
            @Override
            public void usuarioCarregado(clsUsuarios Usuario) {
                if (Usuario == null) {
                    AlertDialog.Builder internet = new AlertDialog.Builder(LoginActivity.this);
                    internet.setTitle(getResources().getString(R.string.erro));
                    internet.setMessage(getResources().getString(R.string.login_erro_conexao_servidor));
                    internet.show();
                }
                else
                {
                    //login com sucesso
                    if (Usuario.idUsuario > 0) {
                        SharedPreferences settings = getSharedPreferences("USUARIO", 0);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putInt("ID_USUARIO", Usuario.idUsuario);
                        editor.apply();

                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    } else if (Usuario.senhaUsuario.equals("INCORRETA")) {
                        //senha incorreta
                        mPasswordView.setError(getResources().getString(R.string.senha_incorreta));
                        mPasswordView.requestFocus();
                    } else if (Usuario.nomeUsuario.equals("INVALIDO")) {
                        mEmailView.setError(getResources().getString(R.string.senha_incorreta));
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                        builder.setTitle(getResources().getString(R.string.erro_desconhecido));
                        builder.setMessage(getResources().getString(R.string.login_erro_conexao_servidor_mais_serio));
                        //se o malandro pressionar fora do AlertDialog, fecha o aplicativo
                        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                finish();
                            }
                        });
                        builder.setPositiveButton(getResources().getString(R.string.repetir), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == DialogInterface.BUTTON_POSITIVE) {
                                    tentarLogin();
                                } else {
                                    finish();
                                }
                            }
                        }).setNegativeButton(getResources().getString(R.string.fechar), null);
                        builder.create().show();
                    }
                }
                showProgress(false);
            }
        });
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


    private void tentarLogin() {

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mUsuarioView.setError(null);

        // Store values at the time of the login attempt.
        String emailouUsuario = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String usuario = mUsuarioView.getText().toString();

        boolean cancel = false;
        boolean cadastrando = mUsuarioView.getHeight() > 0;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !(password.length() > 4)) {
            mPasswordView.setError(getResources().getString(R.string.login_senha_invalida));
            focusView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(emailouUsuario)) {
            mEmailView.setError(getResources().getString(R.string.campo_obrigatorio));
            focusView = mEmailView;
            cancel = true;
        }
        if ((cadastrando) && (TextUtils.isEmpty(usuario))) {
            mUsuarioView.setError(getResources().getString(R.string.campo_obrigatorio));
            focusView = mUsuarioView;
            cancel = true;
        }
        if ((cadastrando) && (!emailouUsuario.contains("@") || !emailouUsuario.contains("."))) {
            mEmailView.setError(getResources().getString(R.string.login_email_invalido));
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
            if (cadastrando)
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
                        mEmailView.setError(getResources().getString(R.string.login_email_ja_cadastrado));
                        mUsuarioView.setError(getResources().getString(R.string.login_email_ja_cadastrado));
                        mEmailView.requestFocus();
                        showProgress(false);
                        break;
                    case "ERRO_DOWNLOAD": {
                        AlertDialog.Builder novo = new AlertDialog.Builder(this);
                        novo.setMessage(getResources().getString(R.string.erro_conexao_confira_internet));
                        novo.setPositiveButton(getResources().getString(R.string.ok), null);
                        novo.show();
                        showProgress(false);
                        break;
                    }
                    default: {
                        AlertDialog.Builder novo = new AlertDialog.Builder(this);
                        novo.setMessage(getResources().getString(R.string.login_erro_conexao_servidor_mais_serio) + getResources().getString(R.string.login_informe_codigo) + "\n" + retornoCadastro);
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
    private void showProgress(final boolean show) {

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
        CheckBox cbxCadastra = (CheckBox) view;
        if(cbxCadastra.isChecked()) {
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            mEmailView.setHint(getResources().getString(R.string.e_mail));
            mUsuarioView.setEnabled(true);
            mEmailView.setNextFocusForwardId(R.id.txt_login_usuario);
        }
        else
        {
            mEmailView.setNextFocusForwardId(R.id.txt_login_senha);
            mUsuarioView.setEnabled(false);
            mUsuarioView.setText("");
            lp.height = 0;
            mEmailView.setHint(getResources().getString(R.string.e_mail_ou_usuario));
        }
        habilitar.setLayoutParams(lp);
    }

    public void txtRecuperaSenha_Click(View view) {
        AlertDialog.Builder dlgRecupera = new AlertDialog.Builder(this);
        dlgRecupera.setTitle(getResources().getString(R.string.recuperar_conta));
        LayoutInflater inflater = this.getLayoutInflater();
        final View recuperaSenha = inflater.inflate(R.layout.layout_recupera_senha, null);
        //adiciona o layout recuperasenha como fonte para visual da view
        dlgRecupera.setView(recuperaSenha);
        dlgRecupera.setPositiveButton(getResources().getString(R.string.enviar), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText nomeOuEmail = (EditText) recuperaSenha.findViewById(R.id.txt_recuperasenha);
                if (nomeOuEmail.getText().toString().equals("")) {
                    nomeOuEmail.setError(getResources().getString(R.string.campo_obrigatorio));
                    nomeOuEmail.requestFocus();
                } else {
                    clsUsuarios.recuperaUsuario(new booleanRetornadoListener() {
                        @Override
                        public void booleanRetornado(boolean retorno) {
                            if (retorno) {
                                Toast.makeText(LoginActivity.this, getResources().getString(R.string.login_senha_enviada), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(LoginActivity.this, getResources().getString(R.string.login_senha_nao_enviada), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, nomeOuEmail.getText().toString(), LoginActivity.this);
                }
            }
        });
        dlgRecupera.setCancelable(true);
        dlgRecupera.show();
    }
}



