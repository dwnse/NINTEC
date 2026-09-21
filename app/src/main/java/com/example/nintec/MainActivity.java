package com.example.nintec;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.nintec.fragments.BranchesFragment;
import com.example.nintec.fragments.CartFragment;
import com.example.nintec.fragments.CatalogFragment;
import com.example.nintec.fragments.HomeFragment;
import com.example.nintec.fragments.ProductDetailFragment;
import com.example.nintec.fragments.ProfileFragment;
import com.example.nintec.managers.CartManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity implements CartManager.CartChangeListener {

    private FragmentManager fragmentManager;
    private BottomNavigationView bottomNavigationView;

    private Fragment homeFragment;
    private Fragment catalogFragment;
    private Fragment branchesFragment;
    private Fragment profileFragment;
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentManager = getSupportFragmentManager();
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        if (savedInstanceState != null) {
            homeFragment = fragmentManager.findFragmentByTag("HOME");
            catalogFragment = fragmentManager.findFragmentByTag("CATALOG");
            branchesFragment = fragmentManager.findFragmentByTag("BRANCHES");
            profileFragment = fragmentManager.findFragmentByTag("PROFILE");
            
            if (homeFragment != null && !homeFragment.isHidden()) activeFragment = homeFragment;
            else if (catalogFragment != null && !catalogFragment.isHidden()) activeFragment = catalogFragment;
            else if (branchesFragment != null && !branchesFragment.isHidden()) activeFragment = branchesFragment;
            else if (profileFragment != null && !profileFragment.isHidden()) activeFragment = profileFragment;
        }

        if (homeFragment == null) homeFragment = new HomeFragment();
        if (catalogFragment == null) catalogFragment = new CatalogFragment();
        if (branchesFragment == null) branchesFragment = new BranchesFragment();
        if (profileFragment == null) profileFragment = new ProfileFragment();

        if (activeFragment == null) {
            fragmentManager.beginTransaction()
                    .add(R.id.fragment_container, homeFragment, "HOME")
                    .commit();
            activeFragment = homeFragment;
        }

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment targetFragment = null;
                String tag = "";
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    targetFragment = homeFragment;
                    tag = "HOME";
                } else if (id == R.id.nav_catalog) {
                    targetFragment = catalogFragment;
                    tag = "CATALOG";
                } else if (id == R.id.nav_branches) {
                    targetFragment = branchesFragment;
                    tag = "BRANCHES";
                } else if (id == R.id.nav_profile) {
                    targetFragment = profileFragment;
                    tag = "PROFILE";
                }

                if (targetFragment == activeFragment) {
                    return true; 
                }

                if (targetFragment != null) {
                    fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    showBottomNavigation();

                    FragmentTransaction ft = fragmentManager.beginTransaction();
                    
                    if (activeFragment != null) {
                        ft.hide(activeFragment);
                    }

                    if (!targetFragment.isAdded()) {
                        ft.add(R.id.fragment_container, targetFragment, tag);
                    } else {
                        ft.show(targetFragment);
                    }

                    ft.commit();
                    activeFragment = targetFragment;
                    return true;
                }
                return false;
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (fragmentManager.getBackStackEntryCount() > 0) {
                    fragmentManager.popBackStack();
                    // showBottomNavigation();
                } else if (activeFragment != homeFragment) {
                    bottomNavigationView.setSelectedItemId(R.id.nav_home);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        CartManager.getInstance().addListener(this);
    }

    @Override
    protected void onDestroy() {
        CartManager.getInstance().removeListener(this);
        super.onDestroy();
    }

    public void openProductDetail(String productId) {
        ProductDetailFragment detailFragment = ProductDetailFragment.newInstance(productId);
        // Ensure bottom navigation stays visible if desired
        // hideBottomNavigation(); 
        
        fragmentManager.beginTransaction()
                .add(R.id.fragment_container, detailFragment, "DETAIL")
                .addToBackStack("DETAIL_TRANS")
                .commit();
    }

    public void openCart() {
        CartFragment cartFragment = new CartFragment();
        // hideBottomNavigation();

        fragmentManager.beginTransaction()
                .add(R.id.fragment_container, cartFragment, "CART")
                .addToBackStack("CART_TRANS")
                .commit();
    }

    public void showBottomNavigation() {
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.VISIBLE);
        }
    }

    public void hideBottomNavigation() {
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCartChanged(int totalItemCount) {
        // This callback is ready for any badge UI notification implementation in future header refinements
    }
}