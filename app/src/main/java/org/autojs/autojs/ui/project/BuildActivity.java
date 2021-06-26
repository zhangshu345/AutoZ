package org.autojs.autojs.ui.project;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;

import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.textfield.TextInputLayout;
import com.stardust.app.DialogUtils;
import com.stardust.autojs.core.image.Colors;
import com.stardust.autojs.project.ProjectConfig;
import com.stardust.autojs.project.SigningConfig;
import com.stardust.pio.PFile;
import com.stardust.pio.PFiles;
import com.stardust.util.IntentUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.autojs.autojs.Pref;
import org.autojs.autojs.R;
import org.autojs.autojs.autojs.build.ApkBuilder;
import org.autojs.autojs.build.ApkBuilderPluginHelper;
import org.autojs.autojs.build.ApkKeyStore;
import org.autojs.autojs.build.ApkSigner;
import org.autojs.autojs.build.apksigner.KeyStoreFileManager;
import org.autojs.autojs.external.fileprovider.AppFileProvider;
import org.autojs.autojs.model.explorer.ExplorerFileItem;
import org.autojs.autojs.model.explorer.Explorers;
import org.autojs.autojs.model.script.ScriptFile;
import org.autojs.autojs.theme.dialog.ThemeColorMaterialDialogBuilder;
import org.autojs.autojs.tool.BitmapTool;
import org.autojs.autojs.ui.BaseActivity;
import org.autojs.autojs.ui.project.SignManageActivity;
import org.autojs.autojs.ui.project.SignManageActivity_;
import org.autojs.autojs.ui.filechooser.FileChooserDialogBuilder;
import org.autojs.autojs.ui.shortcut.ShortcutIconSelectActivity;
import org.autojs.autojs.ui.shortcut.ShortcutIconSelectActivity_;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by Stardust on 2017/10/22.
 */
@EActivity(R.layout.activity_build)
public class BuildActivity extends BaseActivity implements ApkBuilder.ProgressCallback {

    private static final int REQUEST_CODE = 44401;
    private static final int REQUEST_CODE_SPLASH_ICON = 44402;

    public static final String EXTRA_SOURCE = BuildActivity.class.getName() + ".extra_source_file";

    private static final String LOG_TAG = "BuildActivity";
    private static final Pattern REGEX_PACKAGE_NAME = Pattern.compile("^([A-Za-z][A-Za-z\\d_]*\\.)+([A-Za-z][A-Za-z\\d_]*)$");

    @ViewById(R.id.source_path)
    EditText mSourcePath;

    @ViewById(R.id.source_path_container)
    View mSourcePathContainer;

    @ViewById(R.id.output_path)
    EditText mOutputPath;

    @ViewById(R.id.app_name)
    EditText mAppName;

    @ViewById(R.id.package_name)
    EditText mPackageName;

    @ViewById(R.id.version_name)
    EditText mVersionName;

    @ViewById(R.id.version_code)
    EditText mVersionCode;

    @ViewById(R.id.icon)
    ImageView mIcon;

    @ViewById(R.id.app_splash_text)
    EditText mSplashText;

    @ViewById(R.id.app_splash_icon)
    ImageView mSplashIcon;

    @ViewById(R.id.app_config)
    CardView mAppConfig;

    @ViewById(R.id.sign_key_path)
    TextView mAppSignKeyPath;

    private ProjectConfig mProjectConfig;
    private MaterialDialog mProgressDialog;
    private String mSource;
    private String mDirectory;
    private boolean mIsDefaultIcon = true;
    private Bitmap mIconBitmap;
    private Bitmap mSplashIconBitmap;
    // 签名相关
    private ArrayList<ApkKeyStore> mKeyStoreList;
    private ApkKeyStore mKeyStore;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    void setupViews() {
        setToolbarAsBack(getString(R.string.text_build_apk));
        mSource = getIntent().getStringExtra(EXTRA_SOURCE);
        if (mSource != null) {
            mDirectory = mSource;
            setupWithSourceFile(new ScriptFile(mSource));
        }
        checkApkBuilderPlugin();
    }

    private void checkApkBuilderPlugin() {

    }


    private void setupWithSourceFile(ScriptFile file) {
        String dir = file.getParent();
        if (dir.startsWith(getFilesDir().getPath())) {
            dir = Pref.getScriptDirPath();
        }
        mOutputPath.setText(dir);
        mAppName.setText(file.getSimplifiedName());
        mPackageName.setText(getString(R.string.format_default_package_name, System.currentTimeMillis()));
        setSource(file);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

    }

    @Click(R.id.select_source)
    void selectSourceFilePath() {
        String initialDir = new File(mSourcePath.getText().toString()).getParent();

        new FileChooserDialogBuilder(this)
                .title(R.string.text_source_file_path)
                .dir(Environment.getExternalStorageDirectory().getPath(),
                        initialDir == null ? Pref.getScriptDirPath() : initialDir)
                .singleChoice(this::setSource)
                .show();
    }

    private void setSource(File file) {
        if (!file.isDirectory()) {
            mSourcePath.setText(file.getPath());
            mDirectory = file.getParent();
            mProjectConfig = ProjectConfig.fromProjectDir(mDirectory);
            mOutputPath.setText(mDirectory);
        } else {
            mProjectConfig = ProjectConfig.fromProjectDir(file.getPath());
            mSourcePathContainer.setVisibility(View.GONE);
            mOutputPath.setText(new File(mSource, mProjectConfig.getBuildDir()).getPath());
        }
        if (mProjectConfig == null) {
            return;
        }
        mSourcePath.setText(new File(mSource, "").getPath());
//        mSourcePathContainer.setVisibility(View.GONE);
//        mOutputPath.setText(new File(mSource, mProjectConfig.getBuildDir()).getPath());
//        mAppConfig.setVisibility(View.GONE);
        mPackageName.setText(mProjectConfig.getPackageName());
        mVersionName.setText(mProjectConfig.getVersionName());
        mVersionCode.setText("" + mProjectConfig.getVersionCode());
        String icon = mProjectConfig.getIcon();
        if (icon != null) {
            Glide.with(this)
                    .setDefaultRequestOptions(RequestOptions.skipMemoryCacheOf(true).
                            diskCacheStrategy(DiskCacheStrategy.NONE))
                    .load(new File(mDirectory, icon))
                    .into(mIcon);
        }
        // 运行配置
        mSplashText.setText(mProjectConfig.getLaunchConfig().getSplashText());
        String splashIcon = mProjectConfig.getLaunchConfig().getSplashIcon();
        if (splashIcon != null) {
            Glide.with(this)
                    .setDefaultRequestOptions(RequestOptions.skipMemoryCacheOf(true).
                            diskCacheStrategy(DiskCacheStrategy.NONE))
                    .load(new File(mDirectory, splashIcon))
                    .into(mSplashIcon);
        }
        // 签名相关
        SigningConfig signConfig = mProjectConfig.getSigningConfig();
        if (signConfig.getKeyStore() != null && !signConfig.getKeyStore().isEmpty()) {
            mAppSignKeyPath.setText(signConfig.getKeyStore());
            mKeyStore = ApkSigner.loadApkKeyStore(signConfig.getKeyStore());
        }
    }

    @Click(R.id.select_output)
    void selectOutputDirPath() {
        String initialDir = new File(mOutputPath.getText().toString()).exists() ?
                mOutputPath.getText().toString() : Pref.getScriptDirPath();
        new FileChooserDialogBuilder(this)
                .title(R.string.text_output_apk_path)
                .dir(initialDir)
                .chooseDir()
                .singleChoice(dir -> mOutputPath.setText(dir.getPath()))
                .show();
    }

    @Click(R.id.icon)
    void selectIcon() {
        ShortcutIconSelectActivity_.intent(this)
                .startForResult(REQUEST_CODE);
    }

    @Click(R.id.app_splash_icon)
    void selectSplashIcon() {
        ShortcutIconSelectActivity_.intent(this)
                .startForResult(REQUEST_CODE_SPLASH_ICON);
    }

    @Click(R.id.sign_choose)
    void chooseSign() {
        int selectedIndex = 0;
        mKeyStoreList = ApkSigner.loadKeyStore();
        if (mKeyStore != null) {
            selectedIndex = getSelectIndex(mKeyStore);
        }
        DialogUtils.showDialog(new ThemeColorMaterialDialogBuilder(this)
                .title(R.string.text_sign_choose)
                .items(getSignItems())
                .autoDismiss(false)
                .itemsCallbackSingleChoice(selectedIndex, (dialog, itemView, which, text) -> {
                    if (which <= 0) {
                        mKeyStore = null;
                        return true;
                    } else {
                        mKeyStore = mKeyStoreList.get(which - 1);
                        mAppSignKeyPath.setText(mKeyStore.getPath());
                        if (mKeyStore.isVerified()) {
                            return true;
                        }
                        showPasswordInputDialog(mKeyStore, dialog);
                        return false;
                    }
                })
                .negativeText(R.string.cancel)
                .onNegative((d, w) -> d.dismiss())
                .negativeColorRes(R.color.text_color_secondary)
                .neutralText(R.string.text_sign_manage)
                .positiveText(R.string.ok)
                .onPositive((d, w) -> d.dismiss())
                .onNeutral((d, w) -> onSignManageClick())
                .build());
    }

    private List<String> getSignItems() {
        List<String> list = new ArrayList<>();
        list.add("默认签名");
        if (mKeyStoreList != null) {
            for (ApkKeyStore item : mKeyStoreList) {
                list.add(item.getName());
            }
        }
        return list;
    }

    private int getSelectIndex(ApkKeyStore keyStore) {
        int index = 0;
        int len = mKeyStoreList.size();
        for (int i = 1; i <= len; i++) {
            ApkKeyStore item = mKeyStoreList.get(i - 1);
            Log.d(LOG_TAG, "name: ->" + item.getName());
            Log.d(LOG_TAG, "name: =>" + keyStore.getName());
            Log.d(LOG_TAG, "contains: =>" + item.getName().contains(keyStore.getName()));
            if (item.getName().contains(PFiles.getName(keyStore.getName()))) {
                index = i;
                Log.d(LOG_TAG, "index: =>" + index);
                break;
            }
        }
        return index;
    }

    private void onSignManageClick() {
        SignManageActivity_.intent(this).start();
    }

    private Observable<String> showPasswordInputDialog(ApkKeyStore keyStore, MaterialDialog chooseDialog) {
        final PublishSubject<String> input = PublishSubject.create();
        DialogUtils.showDialog(new ThemeColorMaterialDialogBuilder(this).title(R.string.text_sign_password)
                .inputType(InputType.TYPE_TEXT_VARIATION_PASSWORD)
                .autoDismiss(false)
                .canceledOnTouchOutside(false)
                .input(getString(R.string.text_sign_password_input), "", false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {

                    }
                })
                .onPositive((dialog, which) -> {
                    String password = dialog.getInputEditText().getText().toString();
                    if (ApkSigner.checkKeyStore(keyStore.getPath(), password)) {
                        Pref.setKeyStorePassWord(PFiles.getName(keyStore.getPath()), password);
                        dialog.dismiss();
                        if (chooseDialog != null) {
                            chooseDialog.dismiss();
                        }
                        keyStore.setVerified(true);
                        input.onComplete();
                    } else {
                        dialog.getInputEditText().setError("验证失败");
                    }
                })
                .onNeutral((dialog, which) -> {
                    dialog.dismiss();
                })
                .build());
        return input;
    }

    private boolean syncProjectConfig() {
        if (mProjectConfig == null) {
//            new ThemeColorMaterialDialogBuilder(this)
//                    .title(R.string.text_invalid_project_config)
//                    .positiveText(R.string.ok)
//                    .dismissListener(dialogInterface -> finish())checkApkBuilderPlugin
//                    .show();
//            return false;
            mProjectConfig = new ProjectConfig();
            if (PFiles.isFile(mSource)) {
                mProjectConfig.setMainScriptFile(new File(mSource).getName());
            }
        }
        mProjectConfig.setName(mAppName.getText().toString());
        mProjectConfig.setVersionCode(Integer.parseInt(mVersionCode.getText().toString()));
        mProjectConfig.setVersionName(mVersionName.getText().toString());
//        mProjectConfig.setMainScriptFile(mMainFileName.getText().toString());
        mProjectConfig.setPackageName(mPackageName.getText().toString());
        //mProjectConfig.getLaunchConfig().setHideLogs(true);
        mProjectConfig.getLaunchConfig().setSplashText(mSplashText.getText().toString());
        if (mKeyStore != null) {
            mProjectConfig.getSigningConfig().setKeyStore(mKeyStore.getPath());
            mProjectConfig.getSigningConfig().setAlias(mKeyStore.getAlias());
        } else {
            mProjectConfig.getSigningConfig().setKeyStore("");
            mProjectConfig.getSigningConfig().setAlias("");
        }
        return true;
    }

    @SuppressLint("CheckResult")
    private Observable<String> saveIcon(Bitmap b) {
        if (b == null || mProjectConfig == null) {
            return Observable.just("empty").map(s -> s).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }
        return Observable.just(b)
                .map(bitmap -> {
                    String iconPath = mProjectConfig.getIcon();
                    if (iconPath == null) {
                        iconPath = "res/logo.png";
                    }
                    File iconFile = new File(mDirectory, iconPath);
                    PFiles.ensureDir(iconFile.getPath());
                    FileOutputStream fos = new FileOutputStream(iconFile);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.close();
                    return iconPath;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(iconPath -> mProjectConfig.setIcon(iconPath));

    }

    @SuppressLint("CheckResult")
    private Observable<String> saveSplashIcon(Bitmap b) {
        if (b == null || mProjectConfig == null) {
            return Observable.just("empty").map(s -> s).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }
        return Observable.just(b)
                .map(bitmap -> {
                    String iconPath = mProjectConfig.getLaunchConfig().getSplashIcon();
                    if (iconPath == null) {
                        iconPath = "res/splashIcon.png";
                    }
                    File iconFile = new File(mDirectory, iconPath);
                    PFiles.ensureDir(iconFile.getPath());
                    FileOutputStream fos = new FileOutputStream(iconFile);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.close();
                    return iconPath;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(iconPath -> mProjectConfig.getLaunchConfig().setSplashIcon(iconPath));

    }

    @SuppressLint("CheckResult")
    @Click(R.id.fab)
    void buildApk() {
        if (!checkInputs()) {
            return;
        }
        if (mKeyStore != null && !mKeyStore.isVerified()) {
            showPasswordInputDialog(mKeyStore, null)
                    .doOnComplete(this::syncAndBuild).subscribe();
            return;
        }
        syncAndBuild();
    }

    private void syncAndBuild() {
        // 同步配置
        if (syncProjectConfig()) {
            // 保存配置后打包
            Observable.merge(saveIcon(mIconBitmap), saveSplashIcon(mSplashIconBitmap))
                    .doFinally(() -> {
                        writeProjectConfigAndRefreshView();
                        doBuildingApk();
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe();
        }
    }

    private void writeProjectConfigAndRefreshView() {
        PFiles.write(ProjectConfig.configFileOfDir(mDirectory),
                mProjectConfig.toJson());
        ExplorerFileItem item = new ExplorerFileItem(mSource, null);
        Explorers.workspace().notifyItemChanged(item, item);
    }

    private boolean checkInputs() {
        boolean inputValid = true;
        inputValid &= checkNotEmpty(mSourcePath);
        inputValid &= checkNotEmpty(mOutputPath);
        inputValid &= checkNotEmpty(mAppName);
        inputValid &= checkNotEmpty(mSourcePath);
        inputValid &= checkNotEmpty(mVersionCode);
        inputValid &= checkNotEmpty(mVersionName);
        inputValid &= checkPackageNameValid(mPackageName);
        return inputValid;
    }

    private boolean checkPackageNameValid(EditText editText) {
        Editable text = editText.getText();
        String hint = ((TextInputLayout) editText.getParent().getParent()).getHint().toString();
        if (TextUtils.isEmpty(text)) {
            editText.setError(hint + getString(R.string.text_should_not_be_empty));
            return false;
        }
        if (!REGEX_PACKAGE_NAME.matcher(text).matches()) {
            editText.setError(getString(R.string.text_invalid_package_name));
            return false;
        }
        return true;

    }

    private boolean checkNotEmpty(EditText editText) {
        if (!TextUtils.isEmpty(editText.getText()) || !editText.isShown())
            return true;
        // TODO: 2017/12/8 more beautiful ways?
        String hint = ((TextInputLayout) editText.getParent().getParent()).getHint().toString();
        editText.setError(hint + getString(R.string.text_should_not_be_empty));
        return false;
    }

    @SuppressLint("CheckResult")
    private void doBuildingApk() {
        ApkBuilder.AppConfig appConfig = createAppConfig();
        System.out.println(getCacheDir());
        File tmpDir = new File(getCacheDir(), "build/");
        File outApk = new File(mOutputPath.getText().toString(),
                String.format("%s_v%s.apk", appConfig.getAppName(), appConfig.getVersionName()));
        showProgressDialog();
        Observable.fromCallable(() -> callApkBuilder(tmpDir, outApk, appConfig))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(apkBuilder -> onBuildSuccessful(outApk),
                        this::onBuildFailed);
    }

    private ApkBuilder.AppConfig createAppConfig() {
        if (mProjectConfig != null) {
            return ApkBuilder.AppConfig.fromProjectConfig(mSource, mProjectConfig);
        }
        String jsPath = mSourcePath.getText().toString();
        String versionName = mVersionName.getText().toString();
        int versionCode = Integer.parseInt(mVersionCode.getText().toString());
        String appName = mAppName.getText().toString();
        String packageName = mPackageName.getText().toString();
        String splashText = mSplashText.getText().toString();
        return new ApkBuilder.AppConfig()
                .setAppName(appName)
                .setSourcePath(jsPath)
                .setPackageName(packageName)
                .setVersionCode(versionCode)
                .setVersionName(versionName)
                .setIcon(mIsDefaultIcon ? null : (Callable<Bitmap>) () ->
                        BitmapTool.drawableToBitmap(mIcon.getDrawable())
                )
                .setSplashText(splashText)
                .setSplashIcon(mSplashIcon.getDrawable() == null ? null : (Callable<Bitmap>) () ->
                        BitmapTool.drawableToBitmap(mSplashIcon.getDrawable())
                );
    }

    private ApkBuilder callApkBuilder(File tmpDir, File outApk, ApkBuilder.AppConfig appConfig) throws Exception {
        InputStream templateApk = ApkBuilderPluginHelper.openTemplateApk(BuildActivity.this);
        String keyStorePath = null;
        String keyStorePassword = null;
        if (mKeyStore != null) {
            keyStorePath = mKeyStore.getPath();
            keyStorePassword = Pref.getKeyStorePassWord(PFiles.getName(mKeyStore.getPath()));
        }
        return new ApkBuilder(templateApk, outApk, tmpDir.getPath())
                .setProgressCallback(BuildActivity.this)
                .prepare()
                .withConfig(appConfig)
                .build()
                .sign(keyStorePath, keyStorePassword)
                .cleanWorkspace();
    }

    private void showProgressDialog() {
        mProgressDialog = new MaterialDialog.Builder(this)
                .progress(true, 100)
                .content(R.string.text_on_progress)
                .cancelable(false)
                .show();
    }

    private void onBuildFailed(Throwable error) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
        Toast.makeText(this, getString(R.string.text_build_failed) + error.getMessage(), Toast.LENGTH_SHORT).show();
        Log.e(LOG_TAG, "Build failed", error);
    }

    private void onBuildSuccessful(File outApk) {
        mProgressDialog.dismiss();
        mProgressDialog = null;
        new MaterialDialog.Builder(this)
                .title(R.string.text_build_successfully)
                .content(getString(R.string.format_build_successfully, outApk.getPath()))
                .positiveText(R.string.text_install)
                .negativeText(R.string.cancel)
                .onPositive((dialog, which) ->
                        IntentUtil.installApkOrToast(BuildActivity.this, outApk.getPath(), AppFileProvider.AUTHORITY)
                )
                .show();

    }

    @Override
    public void onPrepare(ApkBuilder builder) {
        mProgressDialog.setContent(R.string.apk_builder_prepare);
    }

    @Override
    public void onBuild(ApkBuilder builder) {
        mProgressDialog.setContent(R.string.apk_builder_build);

    }

    @Override
    public void onSign(ApkBuilder builder) {
        mProgressDialog.setContent(R.string.apk_builder_package);

    }

    @Override
    public void onClean(ApkBuilder builder) {
        mProgressDialog.setContent(R.string.apk_builder_clean);
    }

    @SuppressLint("CheckResult")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        Log.d(LOG_TAG, "onActivityResult: " + data);
        if (requestCode == REQUEST_CODE) {
            ShortcutIconSelectActivity.getBitmapFromIntent(getApplicationContext(), data)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(bitmap -> {
                        mIcon.setImageBitmap(bitmap);
                        mIconBitmap = bitmap;
                        mIsDefaultIcon = false;
                    }, Throwable::printStackTrace);
        }

        if (requestCode == REQUEST_CODE_SPLASH_ICON) {
            ShortcutIconSelectActivity.getBitmapFromIntent(getApplicationContext(), data)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(bitmap -> {
                        mSplashIcon.setImageBitmap(bitmap);
                        mSplashIconBitmap = bitmap;
                    }, Throwable::printStackTrace);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

}
