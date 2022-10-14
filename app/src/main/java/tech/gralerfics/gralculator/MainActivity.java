package tech.gralerfics.gralculator;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

// 原 Navigation 实现依赖
//    import androidx.navigation.NavController;
//    import androidx.navigation.Navigation;
//    import androidx.navigation.ui.AppBarConfiguration;
//    import androidx.navigation.ui.NavigationUI;

import tech.gralerfics.gralculator.databinding.ActivityMainBinding;
import tech.gralerfics.gralculator.fragments.calculator.CalculatorFragment;
// import tech.gralerfics.gralculator.fragments.graphing.GraphingFragment;
import tech.gralerfics.gralculator.fragments.settings.SettingsFragment;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();
        setContentView(root);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        initViewPager();
        initBottomNavigationBar();

        /* 原 Navigation 的实现
            // 获取 BottomNavigationView 示例 navView
            BottomNavigationView navView = findViewById(R.id.nav_view);

            // 获取 Navigation 导航配置以创建 appBarConfiguration 配置文件
            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_calculator, R.id.navigation_graphing, R.id.navigation_settings
            ).build();

            // 获取 Navigation 的 hostFragment
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

            // 关联 navController 和 appBarConfiguration 配置文件
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

            // 绑定 navController 到 BottomNavigationView 上
            NavigationUI.setupWithNavController(binding.navView, navController);
        */
    }

    // 以下是为了去掉 ActionBar 而用于替代 Navigation 导航的 Viewpager
    private ViewPager viewPager;
    private BottomNavigationView bottomNavigationView;

    public ViewPager getViewPager() {
        return viewPager;
    }

    private void initViewPager() {
        viewPager = findViewById(R.id.view_pager);

        viewPager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                Fragment fragment = null;

                switch (position) {
                    case 0:
                        fragment = new CalculatorFragment();
                        break;
//                    case 1:
//                        fragment = new GraphingFragment();
//                        break;
                    case 1: // case 2:
                        fragment = new SettingsFragment();
                        break;
                }

                assert fragment != null;
                return fragment;
            }

            @Override
            public int getCount() {
                return 2; // 3;
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                bottomNavigationView.getMenu().getItem(position).setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
    }

    private void initBottomNavigationBar() {
        bottomNavigationView = findViewById(R.id.nav_view);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_calculator:
                        viewPager.setCurrentItem(0);
                        break;
//                    case R.id.navigation_graphing:
//                        viewPager.setCurrentItem(1);
//                        break;
                    case R.id.navigation_settings:
                        viewPager.setCurrentItem(1); // viewPager.setCurrentItem(2);
                        break;
                }
                return false;
            }
        });
    }
}
