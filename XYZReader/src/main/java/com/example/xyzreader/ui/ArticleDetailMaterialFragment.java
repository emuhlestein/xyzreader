package com.example.xyzreader.ui;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;

/**
 * Created by edm on 10/18/2016.
 */

public class ArticleDetailMaterialFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>, OnAppBarLayoutCollapseListener{
    public static final String TAG = ArticleDetailMaterialFragment.class.getSimpleName();
    public static final String ARG_ITEM_ID = "item_id";
    public static final String ARG_CURRENT_POSITION = "current_position";
    public static final String ARG_STARTING_POSITION = "starting_position";

    private View mRootView;
    private ImageView mPhotoView;
    private Cursor mCursor;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private AppBarLayout mAppBarLayout;
    private NestedScrollView mScrollView;
    private long mItemId;
    private int mStartingPosition;
    private int mCurrentPosition;
    private boolean mIsTransitioning;
    private long mBackgroundImageFadeMillis;

    public static ArticleDetailMaterialFragment newInstance(long itemId, int currentPosition, int startingPosition) {
        int spos = startingPosition;
        Bundle args = new Bundle();
        args.putLong(ARG_ITEM_ID, itemId);
        args.putInt(ARG_CURRENT_POSITION, currentPosition);
        args.putInt(ARG_STARTING_POSITION, startingPosition);
        ArticleDetailMaterialFragment fragment = new ArticleDetailMaterialFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mItemId = getArguments().getLong(ARG_ITEM_ID);
        mCurrentPosition = getArguments().getInt(ARG_CURRENT_POSITION);
        mStartingPosition = getArguments().getInt(ARG_STARTING_POSITION);
        mIsTransitioning = savedInstanceState == null && mStartingPosition == mCurrentPosition;
        mBackgroundImageFadeMillis = 1000;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_details_material_layout, container, false);
        mPhotoView = (ImageView) mRootView.findViewById(R.id.photo);
        mCollapsingToolbarLayout = (CollapsingToolbarLayout) mRootView.findViewById(R.id.collapsing_toolbar);
        mCollapsingToolbarLayout.setBackgroundColor(getResources().getColor(R.color.cardview_dark_background));
        mAppBarLayout = (AppBarLayout) mRootView.findViewById(R.id.appbar);
        mAppBarLayout.addOnOffsetChangedListener(new ScrollingHelper(mAppBarLayout.getTotalScrollRange(), this));

        Toolbar toolbar = (Toolbar) mRootView.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity)getActivity();
        activity.setSupportActionBar(toolbar);



        getLoaderManager().initLoader(0, null, this);
        return mRootView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                FragmentManager fm = getActivity().getSupportFragmentManager();
                if(fm.getBackStackEntryCount() > 0) {
                    fm.popBackStack();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startPostponedEnterTransition() {
        if (mCurrentPosition == mStartingPosition) {
            mPhotoView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mPhotoView.getViewTreeObserver().removeOnPreDrawListener(this);
                    getActivity().startPostponedEnterTransition();
                    return true;
                }
            });
        }
    }


    private void bindViews() {
        if (mRootView == null) {
            return;
        }

        TextView titleView = (TextView) mRootView.findViewById(R.id.article_title);
        TextView bylineView = (TextView) mRootView.findViewById(R.id.article_byline);
        bylineView.setMovementMethod(new LinkMovementMethod());
        TextView bodyView = (TextView) mRootView.findViewById(R.id.article_body);
        bodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));

        if (mCursor != null) {
            //mRootView.setAlpha(0);
            //mRootView.setVisibility(View.VISIBLE);
            //mRootView.animate().alpha(1);
            String title = mCursor.getString(ArticleLoader.Query.TITLE);
            String url = mCursor.getString(ArticleLoader.Query.PHOTO_URL);
            titleView.setText(title);
            mPhotoView.setTransitionName(title);
            /*
            Picasso.with(getActivity()).load(url).into(mPhotoView);
            */


            bylineView.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by <font color='#ffffff'>"
                            + mCursor.getString(ArticleLoader.Query.AUTHOR)
                            + "</font>"));
            bodyView.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY)));

            ImageLoaderHelper.getInstance(getActivity()).getImageLoader()
                    .get(mCursor.getString(ArticleLoader.Query.PHOTO_URL), new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                            Bitmap bitmap = imageContainer.getBitmap();
                            startPostponedEnterTransition();
                            if (bitmap != null) {
                                Palette p = Palette.generate(bitmap, 12);
                                //mMutedColor = p.getDarkMutedColor(0xFF333333);
                                mPhotoView.setImageBitmap(imageContainer.getBitmap());
                                //mRootView.findViewById(R.id.meta_bar)
                                //        .setBackgroundColor(mMutedColor);
                                //updateStatusBar();
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            startPostponedEnterTransition();
                        }
                    });
        } else {
            mRootView.setVisibility(View.GONE);
            titleView.setText("N/A");
            bylineView.setText("N/A" );
            bodyView.setText("N/A");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        mCursor = cursor;
        Log.i(TAG, "Column Count: " + mCursor.getColumnCount());
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }

        bindViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursor = null;
        bindViews();
    }

    @Override
    public void onAppBarLayoutCollapse(boolean isCollapsed) {
        if(mCursor == null) {
            return;
        }
        TextView titleView = (TextView) mRootView.findViewById(R.id.article_title);
        if(isCollapsed) {
            String title = mCursor.getString(ArticleLoader.Query.TITLE);
            mCollapsingToolbarLayout.setTitle(" ");
            titleView.setText(title);
        } else {
            String title = mCursor.getString(ArticleLoader.Query.TITLE);
            mCollapsingToolbarLayout.setTitle(title);
            titleView.setText(" ");
        }
    }

    /**
     * Returns the shared element that should be transitioned back to the previous Activity,
     * or null if the view is not visible on the screen.
     */
    @Nullable
    ImageView getPhotoImage() {
        if (isViewInBounds(getActivity().getWindow().getDecorView(), mPhotoView)) {
            return mPhotoView;
        }
        return null;
    }

    /**
     * Returns true if {@param view} is contained within {@param container}'s bounds.
     */
    private static boolean isViewInBounds(@NonNull View container, @NonNull View view) {
        Rect containerBounds = new Rect();
        container.getHitRect(containerBounds);
        return view.getLocalVisibleRect(containerBounds);
    }
}
