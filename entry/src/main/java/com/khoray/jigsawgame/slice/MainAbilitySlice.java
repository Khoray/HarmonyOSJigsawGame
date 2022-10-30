package com.khoray.jigsawgame.slice;

import com.khoray.jigsawgame.ImageItemProvider;
import com.khoray.jigsawgame.ResourceTable;
import com.khoray.jigsawgame.utils.PicCutter;
import com.khoray.jigsawgame.utils.DialogUtil;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.ability.DataAbilityHelper;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;
import ohos.agp.components.*;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.image.ImageSource;
import ohos.media.image.PixelMap;
import ohos.media.photokit.metadata.AVStorage;
import ohos.utils.net.Uri;

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainAbilitySlice extends AbilitySlice {
    static final HiLogLabel label = new HiLogLabel(HiLog.LOG_APP, 0x0001, "选择图片测试");
    private final int imgRequestCode = 123;
    ListContainer lc_img_list;
    Image showChooseImg;
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_main);

        // 设置选择难度picker
        Picker picker = (Picker) findComponentById(ResourceTable.Id_difficulty_picker);
        picker.setMinValue(2); // 设置选择器中的最小值
        picker.setMaxValue(6); // 设置选择器中的最大值
        picker.setFormatter(i -> {
            String value = Integer.toString(i) + "x" + Integer.toString(i);
            return value;
        });

        // 设置开始游戏按钮
        Button startGameBtn = (Button) findComponentById(ResourceTable.Id_start_game_btn);
        startGameBtn.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                if(showChooseImg.getPixelMap() == null) {
                    DialogUtil.showToast(getContext(), "你还没有选择图片");
                    return;
                }
                Intent intent = new Intent();
                intent.setParam("difficulty", picker.getValue());
                intent.setParam("pic", showChooseImg.getPixelMap());
                present(new GameAbilitySlice(), intent);
            }
        });


        // 设置选择图片按钮
        requestPermissionsFromUser(new String[]{"ohos.permission.READ_USER_STORAGE"},imgRequestCode);
        Button btnChooseImg=(Button)findComponentById(ResourceTable.Id_btn_choose_img);
        btnChooseImg.setClickedListener(c->{
            //选择图片
            selectPic();
        });
        showChooseImg=(Image)findComponentById(ResourceTable.Id_show_image);


        // 默认选择图片的listcontainer
        lc_img_list = (ListContainer) findComponentById(ResourceTable.Id_default_image_list);
        List<Integer> imgList = getImageListData();
        ImageItemProvider iip = new ImageItemProvider(imgList, this);
        iip.setListener(new ImageItemProvider.ClickedListener() {
            @Override
            public void click(int pos) {
                showChooseImg.setPixelMap(imgList.get(pos));
                showChooseImg.setPixelMap(PicCutter.rescale(showChooseImg.getPixelMap()));
            }
        });
        lc_img_list.setItemProvider(iip);
    }

    private List<Integer> getImageListData() {
//        List<Integer> ret = new ArrayList<>();
        List<Integer> ret = getResourceByFilePrefix("Media");


        return ret;
    }
    public static List<Integer> getResourceByFilePrefix(String filePrefix) {
        List<Integer> result = new ArrayList<>();
        Arrays.stream(ResourceTable.class.getDeclaredFields()).filter(field -> field.getName().split("_", 2)[0].startsWith(filePrefix)).forEach(field -> {
            field.setAccessible(true);
            try {
                result.add(field.getInt(ResourceTable.class));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        return result;
    }

    private void selectPic() {

        Intent intent = new Intent();
        Operation opt=new Intent.OperationBuilder().withAction("android.intent.action.GET_CONTENT").build();

        intent.setOperation(opt);
        intent.addFlags(Intent.FLAG_NOT_OHOS_COMPONENT);

        intent.setType("image/*");

        startAbilityForResult(intent, imgRequestCode);
    }
    @Override
    protected void onAbilityResult(int requestCode, int resultCode, Intent resultData) {
        HiLog.info(label, "result_code:" + resultCode);
        if(resultCode == -1) {
            if(requestCode==imgRequestCode)
            {
                try {
                    HiLog.info(label,"选择图片getUriString:"+resultData.getUriString());
                } catch (Exception e) {
                    HiLog.info(label,"选择图片getUriString:没有选择图片");
                    e.printStackTrace();
                    return;
                }

                //选择的Img对应的Uri
                String chooseImgUri=resultData.getUriString();
                HiLog.info(label,"选择图片getScheme:"+chooseImgUri.substring(chooseImgUri.lastIndexOf('/')));

                //定义数据能力帮助对象
                DataAbilityHelper helper=DataAbilityHelper.creator(getContext());
                //定义图片来源对象
                ImageSource imageSource = null;
                //获取选择的Img对应的Id
                String chooseImgId=null;
                //如果是选择文件则getUriString结果为content://com.android.providers.media.documents/document/image%3A30，其中%3A是":"的URL编码结果，后面的数字就是image对应的Id
                //如果选择的是图库则getUriString结果为content://media/external/images/media/30，最后就是image对应的Id
                //这里需要判断是选择了文件还是图库
                if(chooseImgUri.lastIndexOf("%3A")!=-1){
                    chooseImgId = chooseImgUri.substring(chooseImgUri.lastIndexOf("%3A")+3);
                }
                else {
                    chooseImgId = chooseImgUri.substring(chooseImgUri.lastIndexOf('/')+1);
                }
                //获取图片对应的uri，由于获取到的前缀是content，我们替换成对应的dataability前缀
                Uri uri=Uri.appendEncodedPathToUri(AVStorage.Images.Media.EXTERNAL_DATA_ABILITY_URI,chooseImgId);
                HiLog.info(label,"选择图片dataability路径:"+uri.toString());
                try {
                    //读取图片
                    FileDescriptor fd = helper.openFile(uri, "r");
                    imageSource = ImageSource.create(fd, null);
                    //创建位图
                    PixelMap pixelMap = imageSource.createPixelmap(null);
                    //设置图片控件对应的位图
                    showChooseImg.setPixelMap(pixelMap);
                    showChooseImg.setPixelMap(PicCutter.rescale(showChooseImg.getPixelMap()));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (imageSource != null) {
                        imageSource.release();
                    }
                }
            }
        }

    }
    @Override
    public void onActive() {
        super.onActive();
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }
}
