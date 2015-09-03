package eu.sourceway.notificationtest;

import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;


class PlaybackNotification {
	static final int NOTIFICATION_ID = 1;

	protected Context context;
	protected NotificationManager notificationManager;

	private NotificationCompat.Builder builder;

	private MediaSessionCompat mediaSession = null;
	private MediaMetadataCompat.Builder mediaMetaDataBuilder = null;


	private Handler handler_ = new Handler(Looper.getMainLooper());

	private final Target target = new ImageTarget();

	PlaybackNotification(Context context) {
		this.context = context;

		notificationManager = ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE));
	}

	public final void create() {
		createBuilder();

		notificationManager.notify(NOTIFICATION_ID, builder.build());
	}

	public void update() {
		if (builder == null) {
			return;
		}

		builder.setContentTitle("title")
				.setContentText("content")
				.setTicker("ticker")
				.setSubText("Subtext");

		handler_.post(
				new Runnable() {
					@Override
					public void run() {
						Picasso.with(context)
								.load("https://api.tb-group.fm/images/release/5555")
								.into(target);
					}
				}
		);


		notificationManager.notify(NOTIFICATION_ID, builder.build());
	}


	protected void updateLargeIcon(Bitmap bitmap) {
		if (builder == null) {
			return;
		}
		Log.w("UPDATE IMAGE", bitmap.getWidth() + " " + bitmap.getHeight());
		builder.setLargeIcon(bitmap);
		mediaSession.setMetadata(
				mediaMetaDataBuilder
						.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
						.build()
		);
		notificationManager.notify(NOTIFICATION_ID, builder.build());
	}

	public void teardown() {
		if (mediaSession != null) {
			mediaSession.release();
		}
		mediaSession = null;
		builder = null;
	}

	private NotificationCompat.Builder createBuilder() {
		builder = new NotificationCompat.Builder(context);
		builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
				.setCategory(NotificationCompat.CATEGORY_TRANSPORT)
				.setSmallIcon(R.mipmap.ic_launcher)
				.setUsesChronometer(true)
				.setAutoCancel(false)
				.setOngoing(true);


		initMediaSession();

		NotificationCompat.MediaStyle style = new NotificationCompat.MediaStyle();
		style.setMediaSession(mediaSession.getSessionToken());

		builder.setStyle(style);

		return builder;
	}

	private void initMediaSession() {
		mediaMetaDataBuilder = new MediaMetadataCompat.Builder()
				.putString(MediaMetadataCompat.METADATA_KEY_TITLE, "Loading")
				.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "hello")
				.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "hello");

		ComponentName receiverComponent = new ComponentName(context, MusicIntentReceiver.class);
		mediaSession = new MediaSessionCompat(context, "wao stream session", receiverComponent, null);
		mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS | MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS);
		mediaSession.setMetadata(mediaMetaDataBuilder.build());
		mediaSession.setActive(true);

		mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
				.setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_STOP)
				.setState(PlaybackStateCompat.STATE_PLAYING, 0, 0)
				.build());
	}


	private class ImageTarget implements Target {
		@Override
		public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
			updateLargeIcon(bitmap);
		}

		@Override
		public void onBitmapFailed(Drawable errorDrawable) {
		}

		@Override
		public void onPrepareLoad(Drawable placeHolderDrawable) {
			// we just dont care...
		}
	}
}
