package com.codingtest.newsapp.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.codingtest.newsapp.R;
import com.codingtest.newsapp.adapter.HeadLineAdapter;
import com.codingtest.newsapp.clicklisteners.SelectListener;
import com.codingtest.newsapp.model.CountryModel;
import com.codingtest.newsapp.model.NewsArticleModel;
import com.codingtest.newsapp.model.NewsResponse;
import com.codingtest.newsapp.utils.AppUtils;
import com.codingtest.newsapp.utils.Constants;
import com.codingtest.newsapp.utils.ProgressDialogUtils;
import com.codingtest.newsapp.utils.Resource;
import com.codingtest.newsapp.viewmodel.HeadlinesViewModel;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.codingtest.newsapp.databinding.ActivityMainBinding;
import com.google.firebase.analytics.FirebaseAnalytics;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity implements SelectListener {


    private ActivityMainBinding binding;
    private HeadlinesViewModel headlineViewModel;
    private ArrayList<NewsArticleModel> newsArticleList = new ArrayList<>();
    private ArrayList<CountryModel> countryList = new ArrayList<>();
    private HeadLineAdapter headLineAdapter;
    String  countryCode,countryName;
    FirebaseAnalytics analytics;
    SharedPreferences sharedPref;
    SharedPreferences.Editor prefEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());
        analytics=FirebaseAnalytics.getInstance(this);
        headlineViewModel = new ViewModelProvider(this).get(HeadlinesViewModel.class);
        setSupportActionBar(binding.toolbar);
        sharedPref = getSharedPreferences("LastSetting",MODE_PRIVATE);
        int spinnerValue = sharedPref.getInt("userChoiceSpinner",-1);
        if(spinnerValue != -1) {
            // set the selected value of the spinner
            binding.spinnerHeadline.setSelection(spinnerValue);
        }




        // Country Dropdown List
        countryList.add(new CountryModel("India", "in"));
        countryList.add(new CountryModel("USA", "us"));
        countryList.add(new CountryModel("Argentina", "ar"));
        countryList.add(new CountryModel("Australia", "au"));
        countryList.add(new CountryModel("Brazil", "br"));
        countryList.add(new CountryModel("China", "cn"));

        // Country set in adapter and selected from dropdown
        ArrayAdapter<CountryModel> countryAdapter=new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item,countryList);
          binding.spinnerHeadline.setAdapter(countryAdapter);
        binding.spinnerHeadline.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                 countryCode = countryList.get(i).countryIsoCode;
                 countryName = countryList.get(i).countryName;
                if (AppUtils.isInternetAvailable(MainActivity.this)) {
                    getNewsArticleData(countryCode, Constants.API_KEY);
                } else {
                    Toast.makeText(MainActivity.this,R.string.please_check_internet_connection,Toast.LENGTH_LONG).show();
                }

                int userChoice = binding.spinnerHeadline.getSelectedItemPosition();
                sharedPref = getSharedPreferences("LastSetting",0);
                prefEditor = sharedPref.edit();
                prefEditor.putInt("userChoiceSpinner",userChoice);
                prefEditor.commit();

            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        initObserver();
        setupAdapter();
    }
    private void getNewsArticleData(String country,String apikey) {

        headlineViewModel.getArticleList(country,apikey);
    }

    /**
     * setup all the observer of  for api call and validations
     */
    private void initObserver(){
        headlineViewModel.responseDataNews.observe(this, new Observer<Resource<NewsResponse>>() {
            @Override
            public void onChanged(Resource<NewsResponse> response) {
                if(response.status != null){
                    switch (response.status){
                        case LOADING: {
                            ProgressDialogUtils.getInstance().show(MainActivity.this);
                            break;
                        }
                        case ERROR:{
                            ProgressDialogUtils.getInstance().dismiss();

                            break;
                        }
                        case SUCCESS:{
                            ProgressDialogUtils.getInstance().dismiss();
                            newsArticleList.clear();
                            List<NewsArticleModel> articles = response.data.getArticles();
                            newsArticleList.addAll(articles);
                            headLineAdapter.notifyDataSetChanged();


                            break;
                        }
                    }
                }
            }
        });
    }
    /**

     * set Adapter for News Article
     */
    private void setupAdapter() {
        headLineAdapter =  new HeadLineAdapter(newsArticleList, getApplicationContext(), MainActivity.this,this);
        binding.recyclerViewHeadlineList.setAdapter(headLineAdapter);
        binding.recyclerViewHeadlineList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        headLineAdapter.notifyDataSetChanged();
    }

    /**

     * Item click Listener for deatil news article
     */
    @Override
    public void OnNewsClicked(NewsArticleModel newsArticleModel) {

        Intent i=new Intent(MainActivity.this, NewsDetailActivity.class);
        Bundle bundle =new Bundle();
        bundle.putSerializable("data",(Serializable)newsArticleModel);
        i.putExtra("BUNDLE",bundle);
        startActivity(i);
    }


}