package fr.myapplication.dc.myapplication.media;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import fr.myapplication.dc.myapplication.R;
import fr.myapplication.dc.myapplication.data.persistence.DataPersistenceManager;
import fr.myapplication.dc.myapplication.media.images.RoundedImageTransform;
import util.Constants;
import util.LoggerHelper;

/**
 * Created by Crono on 07/02/17.
 */

//Todo:Set last update Date on contact in order to update the pictures of the contacts if last update friend_since > last connection friend_since
public class PictureStorage {

    public static final String AVATAR_FILE_FORMAT = ".jpg";

    //for the small avatar pic
    public static final StorageReference STORAGE_AVATAR_REF = FirebaseStorage.getInstance().
            getReferenceFromUrl(Constants.FIREBASE_AVATAR_URL);

    //for the real size image
    public static final StorageReference STORAGE_PICTURE_REF = FirebaseStorage.getInstance().
            getReferenceFromUrl(Constants.FIREBASE_PICTURE_URL);

    public static void uploadAvatar(final String login, final byte[] pictureData,final ProgressDialog dialog){

        //point to the file to upload
        StorageReference mountainsRef = STORAGE_AVATAR_REF.child(getPictureTargetPath(login));

        UploadTask uploadTask = mountainsRef.putBytes(pictureData);

/*        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //Todo: Handle unsuccessful uploads
                LoggerHelper.debug("failure uploading picture");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                LoggerHelper.info("downloadUrl="+downloadUrl.getLastPathSegment());
                DataPersistenceManager.persistPicture(login,getPictureTargetPath(login));
                LoggerHelper.info("dialog dismissed");
                dialog.dismiss();
            }
        });

        if(dialog != null){
            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    @SuppressWarnings("VisibleForTests")
                    long bytes = taskSnapshot.getBytesTransferred();
                    LoggerHelper.info(this.getClass().getName(), "Bytes uploaded: " + bytes);
                    int progress = (int)(100.0 * (taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount()));
                    dialog.setMessage((int) progress + "");
                    dialog.setProgress(progress);
                }
            });
        }*/
    }

    //Todo:handle OnPause event and other oevents
    public static void downloadAvatar(final String picturePath, final ImageView avatar){

        LoggerHelper.debug("PictureStorage.downloadAvatar - picturePath="+picturePath);

        Task<Uri> task = FirebaseStorage.getInstance().getReferenceFromUrl(Constants.FIREBASE_AVATAR_URL).
        child(picturePath).getDownloadUrl();

        task.addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                LoggerHelper.debug("Picture downloaded Successfully");
                avatar.setImageURI(uri);
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                LoggerHelper.debug("Picture download Failed");
            }
        });
    }

    public static String getPictureTargetPath(final String login){
        return Constants.AVATAR_URL_PREFIX + login + AVATAR_FILE_FORMAT;
    }

    public static File getAvatarFile(Context context,final String login) {
        ContextWrapper cw = new ContextWrapper(context);
        File directory = cw.getDir(Constants.IMAGES_PATH, Context.MODE_APPEND);
        return new File(directory, getPictureTargetPath(login));
    }

    public static void loadImageFromStorage(Context context,final String login, ImageView avatarImage) {
        //we are using lastModified as signature
        //in this way glide will not load the file if it didnt change in betwween
        try {
            File f = PictureStorage.getAvatarFile(context,login);
            if (f.exists()) {

                Glide.with(context)
                        .load(new File(f.toURI().getPath()))
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .signature(new StringSignature(String.valueOf(f.lastModified())))// Uri of the picture
                        .transform(new RoundedImageTransform(context))
                        .into(avatarImage);
            }
        } catch (Exception e) {
            LoggerHelper.warn("loadImageFromStorage error : " + e);
        }
    }

    public static void saveToInternalStorage(Context context, final String login, final Bitmap bitmapImage) {
        FileOutputStream fos = null;
        try {
            File f = PictureStorage.getAvatarFile(context,login);
            LoggerHelper.info("avatar path is " + f.getAbsolutePath());
            fos = new FileOutputStream(f);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            LoggerHelper.info("avatar compressed" + f.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                LoggerHelper.warn("saveToInternalStorage error : " + e);
            }
        }
    }


    public static void loadAvatar(final RequestManager glide,final Context context,final String login,final ImageView avatar){
        try {
            final String avatar_path = PictureStorage.getPictureTargetPath(login);
            final StorageReference ref = PictureStorage.STORAGE_AVATAR_REF.child(avatar_path);
            final String signature = String.valueOf(System.currentTimeMillis() / (1000 * 60 * 60)); // updated every hour

            LoggerHelper.info("Signature is " + signature);
            glide
            .using(new FirebaseImageLoader())
            .load(ref)
            .placeholder(R.drawable.icon_default_user)
            .override(300, 200)
            .diskCacheStrategy(DiskCacheStrategy.RESULT)
            .signature(new StringSignature(signature))//
            .transform(new RoundedImageTransform(context))
            .into(avatar);

        } catch (Exception e){
            avatar.setImageResource(R.drawable.icon_default_user);
        }
    }
}
