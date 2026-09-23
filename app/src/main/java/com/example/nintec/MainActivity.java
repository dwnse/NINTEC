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
        com.example.nintec.network.SupabaseClient.init(this);
        com.example.nintec.managers.SessionManager.init(this);
        setContentView(R.layout.activity_main);

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, windowInsets) -> {
            androidx.core.graphics.Insets insets = windowInsets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, insets.top, 0, 0);
            return windowInsets;
        });

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
                    // 1. Clear backstack to close any secondary screens (Product Detail, Cart, etc.)
                    fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    
                    // 2. Ensure secondary fragments added without backstack (like Success screen) are removed.
                    // We can do this by using replace() here OR by specifically removing known tags.
                    // To keep the show/hide strategy for root tabs, we'll use a transaction that cleans up.
                    
                    FragmentTransaction ft = fragmentManager.beginTransaction();
                    
                    // Hide whatever primary tab was active
                    if (activeFragment != null) {
                        ft.hide(activeFragment);
                    }

                    // Remove any existing secondary fragments that might be covering the screen (Success, etc.)
                    Fragment successFrag = fragmentManager.findFragmentByTag("SUCCESS");
                    if (successFrag != null) ft.remove(successFrag);

                    if (!targetFragment.isAdded()) {
                        ft.add(R.id.fragment_container, targetFragment, tag);
                    } else {
                        ft.show(targetFragment);
                    }

                    ft.commit();
                    activeFragment = targetFragment;
                    showBottomNavigation();
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
        fragmentManager.beginTransaction()
                .add(R.id.fragment_container, detailFragment, "DETAIL")
                .addToBackStack("DETAIL_TRANS")
                .commit();
    }

    public void openCatalogWithCategory(String categoryId) {
        if (catalogFragment instanceof CatalogFragment) {
            ((CatalogFragment) catalogFragment).filterByCategory(categoryId);
        }
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_catalog);
        }
    }

    public void openCart() {
        CartFragment cartFragment = new CartFragment();
        // hideBottomNavigation();

        fragmentManager.beginTransaction()
                .add(R.id.fragment_container, cartFragment, "CART")
                .addToBackStack("CART_TRANS")
                .commit();
    }

    public void resetToTab(int itemId) {
        // 1. Clear backstack entries (secondary screens)
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        
        // 2. Ensure bottom bar is visible
        showBottomNavigation();
        
        // 3. Reset active fragment to force the listener to actually perform the switch/show
        activeFragment = null; 
        
        // 4. Select the desired tab
        bottomNavigationView.setSelectedItemId(itemId);
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