package codebrain.com.br.themusicpirate.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import codebrain.com.br.themusicpirate.fragments.MusicsFragment;
import codebrain.com.br.themusicpirate.fragments.MyMusicsFragment;

public class PageAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public PageAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                MusicsFragment tab1 = new MusicsFragment();
                return tab1;
            case 1:
                MyMusicsFragment tab2 = new MyMusicsFragment();
                return tab2;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
