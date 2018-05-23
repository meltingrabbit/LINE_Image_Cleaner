package jp.ac.u_tokyo.line_image_cleaner;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {

	// Toastが連続した場合にcancel出来るようにインスタンス化
	// http://blog.fujiu.jp/2010/12/android-toastcancel.html
	Toast myToast;
//	static String strStatic;
	Handler handler= new Handler();
	// 状態	 0: 未処理, 1: 拡張子付加
	int statue = 0;
	private TextView textView;
	// http://humitomotti.hateblo.jp/entry/2014/09/17/202553
	// http://techbooster.jpn.org/andriod/ui/9564/
	/** プログレスダイアログ */
	private static ProgressDialog waitDialog;
	// ファイルに書き出す際のファイル名
	private final String FILE_NAME = "myfile.txt";

	// R : Recursive
	String pathsNomedia[] = {
			Environment.getExternalStorageDirectory() + "/Android/data/",
			Environment.getExternalStorageDirectory() + "/Android/data/jp.naver.line.android/",
			Environment.getExternalStorageDirectory() + "/Android/data/jp.naver.line.android/storage/gallerybig/",
			Environment.getExternalStorageDirectory() + "/Android/data/jp.naver.line.android/storage/gallerydefault/"
	};
	String pathsJpg[] = {
			Environment.getExternalStorageDirectory() + "/Android/data/jp.naver.line.android/storage/g/",
			Environment.getExternalStorageDirectory() + "/Android/data/jp.naver.line.android/storage/p/",
			Environment.getExternalStorageDirectory() + "/Android/data/jp.naver.line.android/storage/temp/pp/",
			Environment.getExternalStorageDirectory() + "/Android/data/jp.naver.line.android/storage/toyboximg/line/"
	};
	String pathsJpgR[] = {
			Environment.getExternalStorageDirectory() + "/Android/data/jp.naver.line.android/storage/toybox/linealbum/",
			Environment.getExternalStorageDirectory() + "/Android/data/jp.naver.line.android/storage/toyboximg/linealbum/",
			Environment.getExternalStorageDirectory() + "/Android/data/jp.naver.line.android/stickers/"
	};
	String pathsPng[] = {
			Environment.getExternalStorageDirectory() + "/Android/data/jp.naver.line.android/storage/mmicon/",
			Environment.getExternalStorageDirectory() + "/Android/data/jp.naver.line.android/storage/toyboximg/com.linecorp.advertise/"
	};
	String pathsJpgPngR[] = {
			Environment.getExternalStorageDirectory() + "/Android/data/jp.naver.line.android/storage/mo/"
	};

	/**
	 * ATTENTION: This was auto-generated to implement the App Indexing API.
	 * See https://g.co/AppIndexing/AndroidStudio for more information.
	 */
	private GoogleApiClient client;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// インスタンス作成
		waitDialog = new ProgressDialog(this);

		textView = (TextView) findViewById(R.id.textView);
		statue = isStatue();
		if (statue == 0) {
			textView.setText("画像拡張子未付加");
		} else {
			textView.setText("画像拡張子付加済");
		}

		Button buttonAddJpg = (Button) findViewById(R.id.addJpg);
		Button buttonAddPng = (Button) findViewById(R.id.addPng);
		Button buttonRemove = (Button) findViewById(R.id.remove);
		//Button buttonTemp = (Button) findViewById(R.id.buttonTemp);		// テスト用

		// リスナーをボタンに登録
		// テスト用
/*
		buttonTemp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String str = Environment.getExternalStorageDirectory() + "/Android/data/jp.naver.line.android/storage/gallerybig/.nomedia";
				File file = new File(str);
				showMyToast(file.getName() + "\n" + file.getParent());
			}
		});
*/

		buttonAddJpg.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				statue = isStatue();

				if (statue == 0) {
					if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
						showMyToast("ERROR : SDカードが利用できません．");
						finish();
					} else {
						showMyToast("画像拡張子の付加を開始します...\nボタンを押さないでください．");

						// タイトル設定
						waitDialog.setTitle("処理中...");
						// メッセージ設定
						waitDialog.setMessage("画像拡張子付加中...");
						// スタイル設定 スピナー
						waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
						// キャンセル可能か(バックキーでキャンセル）
						waitDialog.setCancelable(false);
						// ダイアログ表示
						waitDialog.show();

						// 別スレッドで時間のかかる処理を実行
						new Thread(new Runnable() {
							@Override
							public void run() {
								int cnt = 0;
								// 時間のかかる処理
								addExtension("jpg");
								// 終わったらダイアログ消去
								waitDialog.dismiss();

								// 書き込み文字列
								String str = "1";

								// 実際の保存処理
								try {
									//ファイルの絶対パスを取得
									String filePath = Environment.getExternalStorageDirectory() + "/" + FILE_NAME;

									// 1行目だけが本体メモリの場合と異なる
									FileOutputStream fos = new FileOutputStream(filePath);
									OutputStreamWriter osw = new OutputStreamWriter(fos);
									BufferedWriter bw = new BufferedWriter(osw);
									bw.write(str);
									bw.flush();
									bw.close();

									/* Handler経由でメインスレッド（UIスレッド）にタスクを投げる */
									handler.post(new Runnable() {
										/**
										 * このメソッドがメインスレッド（UIスレッド）で実行される
										 */
										@Override
										public void run() {
											textView.setText("画像拡張子付加済（Jpg優先）");
											showMyToast("画像拡張子を付加しました．");
										}
									});
								} catch (Exception e) {
									e.printStackTrace();
									/* Handler経由でメインスレッド（UIスレッド）にタスクを投げる */
									handler.post(new Runnable() {
										/**
										 * このメソッドがメインスレッド（UIスレッド）で実行される
										 */
										@Override
										public void run() {
											showMyToast("ERROR : SDカードに保存失敗");
											finish();
										}
									});
								}
							}
						}).start();
					}
				} else {
					showMyToast("すでに画像拡張子が付加されています．");
				}
			}
		});

		buttonAddPng.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				statue = isStatue();

				if (statue == 0) {
					if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
						showMyToast("ERROR : SDカードが利用できません．");
						finish();
					} else {
						showMyToast("画像拡張子の付加を開始します...\nボタンを押さないでください．");

						// タイトル設定
						waitDialog.setTitle("処理中...");
						// メッセージ設定
						waitDialog.setMessage("画像拡張子付加中...");
						// スタイル設定 スピナー
						waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
						// キャンセル可能か(バックキーでキャンセル）
						waitDialog.setCancelable(false);
						// ダイアログ表示
						waitDialog.show();

						// 別スレッドで時間のかかる処理を実行
						new Thread(new Runnable() {
							@Override
							public void run() {
								int cnt = 0;
								// 時間のかかる処理
								addExtension("png");
								// 終わったらダイアログ消去
								waitDialog.dismiss();

								// 書き込み文字列
								String str = "1";

								// 実際の保存処理
								try {
									//ファイルの絶対パスを取得
									String filePath = Environment.getExternalStorageDirectory() + "/" + FILE_NAME;

									// 1行目だけが本体メモリの場合と異なる
									FileOutputStream fos = new FileOutputStream(filePath);
									OutputStreamWriter osw = new OutputStreamWriter(fos);
									BufferedWriter bw = new BufferedWriter(osw);
									bw.write(str);
									bw.flush();
									bw.close();

									/* Handler経由でメインスレッド（UIスレッド）にタスクを投げる */
									handler.post(new Runnable() {
										/**
										 * このメソッドがメインスレッド（UIスレッド）で実行される
										 */
										@Override
										public void run() {
											textView.setText("画像拡張子付加済（Png優先）");
											showMyToast("画像拡張子を付加しました．");
										}
									});
								} catch (Exception e) {
									e.printStackTrace();
									/* Handler経由でメインスレッド（UIスレッド）にタスクを投げる */
									handler.post(new Runnable() {
										/**
										 * このメソッドがメインスレッド（UIスレッド）で実行される
										 */
										@Override
										public void run() {
											showMyToast("ERROR : SDカードに保存失敗");
											finish();
										}
									});
								}
							}
						}).start();
					}
				} else {
					showMyToast("すでに画像拡張子が付加されています．");
				}
			}
		});

		buttonRemove.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				statue = isStatue();

				if (statue == 1) {
					if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
						showMyToast("ERROR : SDカードが利用できません．");
						finish();
					} else {
						showMyToast("画像拡張子の除去を開始します...\nボタンを押さないでください．");

						// タイトル設定
						waitDialog.setTitle("処理中...");
						// メッセージ設定
						waitDialog.setMessage("画像拡張子除去中...");
						// スタイル設定 スピナー
						waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
						// キャンセル可能か(バックキーでキャンセル）
						waitDialog.setCancelable(false);
						// ダイアログ表示
						waitDialog.show();

						// 別スレッドで時間のかかる処理を実行
						new Thread(new Runnable() {
							@Override
							public void run() {
								int cnt = 0;
								// 時間のかかる処理
								removeExtension();
								// 終わったらダイアログ消去
								waitDialog.dismiss();

								// 書き込み文字列
								String str = "0";

								/* 実際の保存処理 */
								try {
									/* ファイルの絶対パスを取得 */
									String filePath = Environment.getExternalStorageDirectory() + "/" + FILE_NAME;

									/* 1行目だけが本体メモリの場合と異なる */
									FileOutputStream fos = new FileOutputStream(filePath);
									OutputStreamWriter osw = new OutputStreamWriter(fos);
									BufferedWriter bw = new BufferedWriter(osw);
									bw.write(str);
									bw.flush();
									bw.close();

									/* Handler経由でメインスレッド（UIスレッド）にタスクを投げる */
									handler.post(new Runnable() {
										/**
										 * このメソッドがメインスレッド（UIスレッド）で実行される
										 */
										@Override
										public void run() {
											textView.setText("画像拡張子未付加");
											showMyToast("画像拡張子を除去しました．");
										}
									});
								} catch (Exception e) {
									e.printStackTrace();
											/* Handler経由でメインスレッド（UIスレッド）にタスクを投げる */
									handler.post(new Runnable() {
										/**
										 * このメソッドがメインスレッド（UIスレッド）で実行される
										 */
										@Override
										public void run() {
											showMyToast("ERROR : SDカードに保存失敗");
											finish();
										}
									});
								}
							}
						}).start();
					}
				} else {
					showMyToast("画像拡張子は付加されていません．");
				}
			}
		});

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
	}

	@Override
	public void onStart() {
		super.onStart();

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client.connect();
		Action viewAction = Action.newAction(
				Action.TYPE_VIEW, // TODO: choose an action type.
				"Main Page", // TODO: Define a title for the content shown.
				// TODO: If you have web page content that matches this app activity's content,
				// make sure this auto-generated web page URL is correct.
				// Otherwise, set the URL to null.
				Uri.parse("http://host/path"),
				// TODO: Make sure this auto-generated app deep link URI is correct.
				Uri.parse("android-app://jp.ac.u_tokyo.line_image_cleaner/http/host/path")
		);
		AppIndex.AppIndexApi.start(client, viewAction);
	}

	@Override
	public void onStop() {
		super.onStop();

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		Action viewAction = Action.newAction(
				Action.TYPE_VIEW, // TODO: choose an action type.
				"Main Page", // TODO: Define a title for the content shown.
				// TODO: If you have web page content that matches this app activity's content,
				// make sure this auto-generated web page URL is correct.
				// Otherwise, set the URL to null.
				Uri.parse("http://host/path"),
				// TODO: Make sure this auto-generated app deep link URI is correct.
				Uri.parse("android-app://jp.ac.u_tokyo.line_image_cleaner/http/host/path")
		);
		AppIndex.AppIndexApi.end(client, viewAction);
		client.disconnect();
	}


	public int isStatue() {
		/* SDカードが利用可能かチェック */
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			showMyToast("ERROR : SDカードが利用できません．");
			finish();
			return -1;
		} else {
			/* 読み込んだデータを一時的に蓄えるためのバッファ */
			StringBuffer sb = new StringBuffer();

			/* 実際の読み込み処理 */
			try {
				/* ファイルの絶対パスを取得 */
				String filePath = Environment.getExternalStorageDirectory() + "/" + FILE_NAME;

				// ファイルが存在するか？
				File file = new File(filePath);
				if ((isFileExist(file)) == 0) {
					return 0;
				}

				/* 1行目だけが本体メモリの場合と異なる */
				FileInputStream fis = new FileInputStream(filePath);
				InputStreamReader isr = new InputStreamReader(fis);
				BufferedReader br = new BufferedReader(isr);

				/* 1行ずつ読み込む */
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line);
					sb.append(System.getProperty("line.separator"));
				}
				br.close();

				String str = sb.toString();

				// 改行コード除去
				String kaigyo = System.getProperty("line.separator");
				str = str.replaceAll(kaigyo, "");

				if (str.equals("1")) {
					return 1;
				} else if (str.equals("0")) {
					return 0;
				} else {
					showMyToast("ERROR : SD読み取り失敗");
					finish();
					return -1;
				}

			} catch (Exception e) {
				e.printStackTrace();
				showMyToast("ERROR : SD読み取り失敗");
				finish();
				return -1;
			}
		}
	}


	public void changeFileName(File file1, File file2) {
		try {
			file1.renameTo(file2);
			/*if (file1.renameTo(file2)) {
				System.out.println("移動成功");
			} else {
				System.out.println("移動失敗");
			}*/
		} catch (SecurityException e) {
			showMyToast("ERROR : 例外");
			finish();
		} catch (NullPointerException e) {
			showMyToast("ERROR : 例外");
			finish();
		}
		return;
	}


	public void addExtension(String imageFormat) {
		// nomedia
		strStatic = ".nomedia を処理中...";

		/* Handler経由でメインスレッド（UIスレッド）にタスクを投げる */
		handler.post(new Runnable() {
			/**
			 * このメソッドがメインスレッド（UIスレッド）で実行される
			 */
			@Override
			public void run() {
				showMyToast(strStatic);
			}
		});

		for (int i = 0 ; i < pathsNomedia.length ; i++) {
			disableNomedia(pathsNomedia[i] + ".nomedia");
		}

		// jpg
		for (int i = 0 ; i < pathsJpg.length ; i++) {
			addJpgPng(pathsJpg[i], "jpg");
		}

		// jpg recursive
		for (int i = 0 ; i < pathsJpgR.length ; i++) {
			addJpgPngR(pathsJpgR[i], "jpg");
		}

		// png
		for (int i = 0 ; i < pathsPng.length ; i++) {
			addJpgPng(pathsPng[i], "png");
		}

		// jpg png recursive
		for (int i = 0 ; i < pathsJpgPngR.length ; i++) {
			addJpgPngR(pathsJpgPngR[i], imageFormat);
		}
	}


	public void addJpgPng(String str, String format) {
		strStatic = str + "/ を処理中...";

		/* Handler経由でメインスレッド（UIスレッド）にタスクを投げる */
		handler.post(new Runnable() {
			/**
			 * このメソッドがメインスレッド（UIスレッド）で実行される
			 */
			@Override
			public void run() {
				showMyToast(strStatic);
			}
		});

		File directory = new File(str);
		// フォルダか？
		if (directory.isDirectory()) {
			File[] filelist = directory.listFiles();
			for (int i = 0 ; i < filelist.length ; i++) {
				File file1 = filelist[i];
				File file2 = new File(file1.getParent() + "/" + file1.getName() + "." + format);
				changeFileName(file1, file2);
			}
		}
		return;
	}


	public void addJpgPngR(String str, String format) {
		File directory = new File(str);
		// フォルダか？
		if (directory.isDirectory()) {
			File[] subDirectories = directory.listFiles();
			for (int j = 0; j < subDirectories.length; j++) {
				if (subDirectories[j].isDirectory()) {

					strStatic = subDirectories[j].getPath() + "/ を処理中...";

					/* Handler経由でメインスレッド（UIスレッド）にタスクを投げる */
					handler.post(new Runnable() {
						/**
						 * このメソッドがメインスレッド（UIスレッド）で実行される
						 */
						@Override
						public void run() {
							showMyToast(strStatic);
						}
					});

					File[] filelist = subDirectories[j].listFiles();
					for (int i = 0 ; i < filelist.length ; i++) {
						File file1 = filelist[i];
						File file2 = new File(file1.getParent() + "/" + file1.getName() + "." + format);
						changeFileName(file1, file2);
					}
				}
			}
		}
		return;
	}


	public void disableNomedia(String str) {
		File file1 = new File(str);
		File file2 = new File(file1.getParent() + "/disable" + file1.getName() + "disable");
		if (isFileExist(file1) ==0) {
			return;
		}
		changeFileName(file1, file2);
		return;
	}

	public void removeExtension() {
		// nomedia
		strStatic = ".nomedia を処理中...";

		/* Handler経由でメインスレッド（UIスレッド）にタスクを投げる */
		handler.post(new Runnable() {
			/**
			 * このメソッドがメインスレッド（UIスレッド）で実行される
			 */
			@Override
			public void run() {
				showMyToast(strStatic);
			}
		});

		for (int i = 0 ; i < pathsNomedia.length ; i++) {
			enableNomedia(pathsNomedia[i] + "disable.nomediadisable");
		}

		// jpg
		for (int i = 0 ; i < pathsJpg.length ; i++) {
			removeJpgPng(pathsJpg[i]);
		}

		// jpg recursive
		for (int i = 0 ; i < pathsJpgR.length ; i++) {
			removeJpgPngR(pathsJpgR[i]);
		}

		// png
		for (int i = 0 ; i < pathsPng.length ; i++) {
			removeJpgPng(pathsPng[i]);
		}

		// png recursive
		for (int i = 0 ; i < pathsJpgPngR.length ; i++) {
			removeJpgPngR(pathsJpgPngR[i]);
		}

	}


	public void removeJpgPng(final String str) {
		final String strStatic = str + "/ を処理中...";

		/* Handler経由でメインスレッド（UIスレッド）にタスクを投げる */
		handler.post(new Runnable() {
			/**
			 * このメソッドがメインスレッド（UIスレッド）で実行される
			 */
			@Override
			public void run() {
				showMyToast(strStatic);
			}
		});

		File directory = new File(str);
		// フォルダか？
		if (directory.isDirectory()) {
			File[] filelist = directory.listFiles();
			for (int i = 0 ; i < filelist.length ; i++) {
				File file1 = filelist[i];
				File file2 = new File(file1.getParent() + "/" + file1.getName().substring(0, file1.getName().length() - 3 ));
				changeFileName(file1, file2);
			}
		}
		return;
	}


	public void removeJpgPngR(String str) {
		File directory = new File(str);
		// フォルダか？
		if (directory.isDirectory()) {
			File[] subDirectories = directory.listFiles();
			for (int j = 0; j < subDirectories.length; j++) {
				if (subDirectories[j].isDirectory()) {

					strStatic = subDirectories[j].getPath() + "/ を処理中...";

					/* Handler経由でメインスレッド（UIスレッド）にタスクを投げる */
					handler.post(new Runnable() {
						/**
						 * このメソッドがメインスレッド（UIスレッド）で実行される
						 */
						@Override
						public void run() {
							showMyToast(strStatic);
						}
					});

					File[] filelist = subDirectories[j].listFiles();
					for (int i = 0; i < filelist.length; i++) {
						File file1 = filelist[i];
						File file2 = new File(file1.getParent() + "/" + file1.getName().substring(0, file1.getName().length() - 3));
						changeFileName(file1, file2);
					}
				}
			}
		}
		return;
	}


	public void enableNomedia(String str) {
		File file1 = new File(str);
		File file2 = new File(file1.getParent() + "/.nomedia");
		if (isFileExist(file1) ==0) {
			return;
		}
		changeFileName(file1, file2);
		return;
	}


	public void showMyToast(String str) {
		if (myToast != null) {
			myToast.cancel();
		}
		myToast = Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT);
		myToast.show();
	}


	public int isFileExist(File file) {
		if (file.exists()){
			return 1;
		}else{
			//showMyToast("NAI");
			return 0;
		}
	}

}


