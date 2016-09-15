package com.harsu.developer.bias;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import helper.AccountClickListener;
import helper.AccountsSet;
import helper.AccountsTableManager;
import helper.LoginController;
import helper.StatusStorer;
import helper.VerticalSpaceItemDecoration;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AccountClickListener, LoginController.ConnectionListener {

    private static final int NEW_ACCOUNT = 1201;
    ArrayList<AccountsSet> data;
    StatusStorer statusStorer;
    Button actionB;
    TextView statusTV;
    RecyclerView mRecyclerView;
    AccountsAdapter mAdapter;
    AccountsTableManager mTableManager;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mAdapter = new AccountsAdapter(this);
        mTableManager = new AccountsTableManager(this);

        reloadData();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(20));
        mAdapter.setClickListener(this);

        final SharedPreferences sharedPreferences = getSharedPreferences(LoginActivity.CREDENTIALS, MODE_PRIVATE);
        boolean autoConnect = sharedPreferences.getBoolean("autoConnect", true);
        SwitchCompat autoConnectSwitch = (SwitchCompat) findViewById(R.id.autoConnect);
        autoConnectSwitch.setChecked(autoConnect);

        autoConnectSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("autoConnect", b);
                editor.apply();
            }
        });


        actionB = (Button) findViewById(R.id.actionButton);
        statusStorer = StatusStorer.getInstance();
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        statusTV = (TextView) findViewById(R.id.status);
        statusTV.setText(statusStorer.getStatusText());
        if (statusStorer.getState() == StatusStorer.State.active) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.INVISIBLE);
        }
        setAction(statusStorer.getStatus());
        statusStorer.setStatusListener(new StatusStorer.StatusListener() {
            @Override
            public void newStatus(String status) {
                statusTV.setText(status);
                setAction(statusStorer.getStatus());
                reloadData();
            }

            @Override
            public void newState(StatusStorer.State state) {
                if (state == StatusStorer.State.active) {
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                }
                setAction(statusStorer.getStatus());
            }
        });

        actionB.setOnClickListener(this);
        if (getIntent().getIntExtra("extra", 0) == 1) {
            onClick(actionB);
        }
        checkLoggedInID();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.addNewAccount);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(MainActivity.this, AddAccountActivity.class), NEW_ACCOUNT);
            }
        });
    }

    private void checkLoggedInID() {
        statusStorer.setState(StatusStorer.State.active);
        LoginController.getLogInID(new LoginController.ConnectionListener() {
            @Override
            public void success() {
                reloadData();
                statusStorer.setStatus(StatusStorer.Status.ALREADY_LOGGED_IN, context);
                statusStorer.setState(StatusStorer.State.dormant);
            }

            @Override
            public void error(LoginController.Error error) {
                statusStorer.setState(StatusStorer.State.dormant);
                if (error == LoginController.Error.WRONG_WIFI)
                    statusStorer.setStatus(StatusStorer.Status.NOT_BITS_NETWORK, context);
                else {
                    statusStorer.setStatus(StatusStorer.Status.CONNECTED, context);
                }
            }
        }, this);

    }


    private void setAction(StatusStorer.Status status) {
        switch (status) {
            case ERROR_LOGGING:
                actionB.setText("Retry");
                actionB.setVisibility(View.VISIBLE);
                break;
            case NOT_BITS_NETWORK:
                actionB.setText("");
                actionB.setVisibility(View.GONE);
                break;
            case CONNECTED:
                if (statusStorer.getState() == StatusStorer.State.active) {
                    actionB.setText("");
                    actionB.setVisibility(View.GONE);
                } else {
                    actionB.setText("Login");
                    actionB.setVisibility(View.VISIBLE);
                }
                break;
            case LOGGING_IN:
                actionB.setText("");
                actionB.setVisibility(View.GONE);
                break;
            case LOGGED_IN:
                actionB.setText("Logout");
                actionB.setVisibility(View.VISIBLE);
                break;
            case DISCONNECTED:
                actionB.setText("");
                actionB.setVisibility(View.GONE);
                actionB.setVisibility(View.GONE);
                break;
            case ALREADY_LOGGED_IN:
                actionB.setText("Logout");
                actionB.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        statusStorer.setStatusListener(null);
        mTableManager.setLoggedIn("");
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.info) {
            startActivity(new Intent(this, InfoActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.actionButton) {
            switch (statusStorer.getStatus()) {
                case ERROR_LOGGING:
                case CONNECTED:
                    statusStorer.setState(StatusStorer.State.active);
                    LoginController.login(this, this);
                    break;

                case LOGGED_IN:
                case ALREADY_LOGGED_IN:
                    statusStorer.setState(StatusStorer.State.active);
                    LoginController.logout(this, new LoginController.ConnectionListener() {
                        @Override
                        public void success() {

                            statusStorer.setState(StatusStorer.State.dormant);
                            statusStorer.status = StatusStorer.Status.CONNECTED;
                            statusStorer.setStatus(StatusStorer.Status.CONNECTED, context);
                            statusTV.setText("Logged Out");
                            mTableManager.setLoggedIn("");
                            reloadData();
                        }

                        @Override
                        public void error(LoginController.Error error) {
                            statusStorer.setState(StatusStorer.State.dormant);
                            statusTV.setText("Error Logging Out.");
                        }
                    });
                    break;
                case NOT_BITS_NETWORK:
                case DISCONNECTED:
                case LOGGING_IN:
                    break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == NEW_ACCOUNT) {
            if (resultCode == RESULT_OK) {
                reloadData();

            }
        }
    }

    private void reloadData() {
        data = mTableManager.getAccounts();
        mAdapter.setData(data);
    }

    @Override
    public void onDeleteClick(int position) {
        final AccountsSet accountsSet = data.get(position);
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setMessage("Are you sure you want to delete the account " + accountsSet.getUsername() + "?");
        dialog.setTitle("Delete Account");
        dialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (data.size() == 1) {
                    mTableManager.delete(accountsSet.getId());
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    MainActivity.this.finish();

                } else {
                    if (accountsSet.getPreference() == 1) {
                        for (AccountsSet set : data) {          //changing preference(random), as it is highly required.
                            if (set.getId() != accountsSet.getId()) {
                                mTableManager.setPreference(set.getId());
                                break;
                            }
                        }
                    }
                    mTableManager.delete(accountsSet.getId());
                    reloadData();
                }
            }
        });
        dialog.setNegativeButton("Cancel", null);
        dialog.show();

    }

    @Override
    public void onLongClick(final int position) {
        final AccountsSet accountsSet = data.get(position);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(accountsSet.getUsername());
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                MainActivity.this,
                android.R.layout.select_dialog_item);
        arrayAdapter.add("Set as Preferred Account");
        arrayAdapter.add("Log In using the Account");
        arrayAdapter.add("Delete the Account");


        alertDialog.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 1:
                        //login
                        onConnectClick(position);
                        return;
                    case 2:
                        //delete
                        onDeleteClick(position);
                        return;
                    case 0:
                        mTableManager.setPreference(accountsSet.getId());
                        reloadData();
                        return;
                }
            }
        });
        alertDialog.show();
    }

    @Override
    public void onConnectClick(int position) {
        statusStorer.setState(StatusStorer.State.active);
        LoginController.login(data.get(position), this, this);
    }

    @Override
    public void success() {
        statusStorer.setState(StatusStorer.State.dormant);
        checkLoggedInID();
    }

    @Override
    public void error(LoginController.Error error) {
        statusStorer.setState(StatusStorer.State.dormant);
        statusStorer.setStatus(StatusStorer.Status.ERROR_LOGGING, context);
    }


    class AccountsAdapter extends RecyclerView.Adapter<AccountsAdapter.MyViewHolder> {

        Context context;
        LayoutInflater inflater;
        ArrayList<AccountsSet> data;
        AccountClickListener clickListener;

        public AccountsAdapter(Context context) {
            this.context = context;
            this.inflater = LayoutInflater.from(context);
            data = new ArrayList<>();
        }

        public void setClickListener(AccountClickListener clickListener) {
            this.clickListener = clickListener;
        }

        public void setData(ArrayList<AccountsSet> data) {
            this.data = data;
            notifyDataSetChanged();
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MyViewHolder(inflater.inflate(R.layout.accounts_row, parent, false));
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            holder.bind(data.get(position));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {

            TextView username;
            ImageView delete, connect;

            MyViewHolder(View itemView) {
                super(itemView);
                username = (TextView) itemView.findViewById(R.id.account);
                delete = (ImageView) itemView.findViewById(R.id.delete);
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (clickListener != null) {
                            clickListener.onDeleteClick(getLayoutPosition());
                        }
                    }
                });
                connect = (ImageView) itemView.findViewById(R.id.connect);
                connect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (clickListener != null) {
                            clickListener.onConnectClick(getLayoutPosition());
                        }
                    }
                });
                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        if (clickListener != null) {
                            clickListener.onLongClick(getLayoutPosition());
                            return true;
                        }
                        return false;
                    }
                });
            }

            void bind(AccountsSet accountsSet) {
                username.setText(accountsSet.getUsername());
                if (accountsSet.getLoggedIn() == 1) {
                    username.setTextColor(Color.parseColor("#4CAF50"));
                    if (accountsSet.getPreference() == 1) {
                        username.setTypeface(null, Typeface.BOLD);
                    } else
                        username.setTypeface(null, Typeface.NORMAL);
                } else {
                    if (accountsSet.getPreference() == 1) {
                        username.setTextColor(Color.parseColor("#000000"));
                        username.setTypeface(null, Typeface.BOLD);
                    } else {
                        username.setTypeface(null, Typeface.NORMAL);
                        username.setTextColor(getResources().getColor(android.R.color.secondary_text_light));
                    }
                }
            }
        }
    }
}
