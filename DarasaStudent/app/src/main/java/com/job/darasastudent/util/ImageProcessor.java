package com.job.darasastudent.util;


import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;

import com.job.darasastudent.R;
import com.job.darasastudent.appexecutor.DefaultExecutorSupplier;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Job on Thursday : 5/3/2018.
 */
public class ImageProcessor {

    private Context context;

    public ImageProcessor(Context context) {
        this.context = context;
    }


    //set images to CircleImageView
    public void setMyImage(final CircleImageView circleImageView, final String url) {

        DefaultExecutorSupplier.getInstance().forMainThreadTasks()
                .execute(new Runnable() {
                    @Override
                    public void run() {

                        if (url == null || url.isEmpty()) {
                            circleImageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.avatar_placeholder));
                        } else {

                            Picasso
                                    .get()
                                    .load(url)
                                    .placeholder(R.drawable.avatar_placeholder)
                                    .error(R.drawable.avatar_placeholder)
                                    .into(circleImageView, new Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            //no cache download new image
                                            Picasso
                                                    .get()
                                                    .load(url)
                                                    .placeholder(R.drawable.avatar_placeholder)
                                                    .error(R.drawable.avatar_placeholder)
                                                    .into(circleImageView);
                                        }
                                    });
                        }
                    }
                });

    }

    //set images to ImageView
    public void setMyImage(final ImageView imageView, final String url) {
        if (url.isEmpty()) {
            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.avatar_placeholder));
        } else {
            Picasso
                    .get()
                    .load(url)
                    .placeholder(R.drawable.avatar_placeholder)
                    .error(R.drawable.avatar_placeholder)
                    .into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            //no cache download new image
                            Picasso
                                    .get()
                                    .load(url)
                                    .placeholder(R.drawable.avatar_placeholder)
                                    .error(R.drawable.avatar_placeholder)
                                    .into(imageView);
                        }
                    });
        }
    }

}
