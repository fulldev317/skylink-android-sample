package sg.com.temasys.skylink.sdk.sampleapp.filetransfer;

import android.content.Context;
import android.util.Log;

import java.io.File;

import sg.com.temasys.skylink.sdk.sampleapp.BasePresenter;
import sg.com.temasys.skylink.sdk.sampleapp.R;
import sg.com.temasys.skylink.sdk.sampleapp.service.FileTransferService;
import sg.com.temasys.skylink.sdk.sampleapp.service.model.PermRequesterInfo;
import sg.com.temasys.skylink.sdk.sampleapp.service.model.SkylinkPeer;
import sg.com.temasys.skylink.sdk.sampleapp.utils.Constants;
import sg.com.temasys.skylink.sdk.sampleapp.utils.PermissionUtils;
import sg.com.temasys.skylink.sdk.sampleapp.utils.Utils;

import static sg.com.temasys.skylink.sdk.sampleapp.utils.Utils.SAMPLE_FILE_NAME;
import static sg.com.temasys.skylink.sdk.sampleapp.utils.Utils.toastLog;

/**
 * Created by muoi.pham on 20/07/18.
 * This class is responsible for implementing file transfer logic.
 */

public class FileTransferPresenter extends BasePresenter implements FileTransferContract.Presenter {

    private final String TAG = FileTransferPresenter.class.getName();

    private Context context;

    // view instance
    private FileTransferContract.View fileTransferView;

    // service instance
    private FileTransferService fileTransferService;

    //utils to process permission
    private PermissionUtils permissionUtils;

    // the index of the peer on the action bar that user selected to send message privately
    // default is 0 - send message to all peers
    private int selectedPeerIndex = 0;

    public FileTransferPresenter(Context context) {
        this.context = context;
        this.fileTransferService = new FileTransferService(context);
        this.fileTransferService.setPresenter(this);
        this.permissionUtils = new PermissionUtils();
    }

    public void setView(FileTransferContract.View view) {
        fileTransferView = view;
        fileTransferView.setPresenter(this);
    }

    /**
     * Triggered when View request data to display to the user when entering room
     * Try to connect to room when entering room
     */
    @Override
    public void onViewRequestConnectedLayout() {

        Log.d(TAG, "onViewLayoutRequested");

        //start to connect to room when entering room
        //if not being connected, then connect
        if (!fileTransferService.isConnectingOrConnected()) {

            //reset permission request states.
            permissionUtils.permQReset();

            //connect to room on Skylink connection
            fileTransferService.connectToRoom(Constants.CONFIG_TYPE.FILE);

            //after connected to skylink SDK, UI will be updated later on onServiceRequestConnect

            Log.d(TAG, "Try to connect when entering room");

        }
    }

    /**
     * process file permission that comes from the app
     * when user first choose browsing file from device, permission request dialog will be display
     */
    @Override
    public boolean onViewRequestFilePermission() {
        return permissionUtils.requestFilePermission(context, fileTransferView.onPresenterRequestGetFragmentInstance());
    }

    /**
     * display a warning if user deny the file permission
     */
    @Override
    public void onViewRequestPermissionDeny() {
        permissionUtils.displayFilePermissionWarning(context);
    }

    /**
     * process result of permission that comes from SDK
     */
    @Override
    public void onViewRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // delegate to the PermissionUtils to process the permission
        permissionUtils.onRequestPermissionsResultHandler(requestCode, permissions, grantResults, TAG);
    }

    /**
     * Sends a file to a Peer or all Peers in room base on the selectedPeerIndex that user
     * selected before
     */
    @Override
    public void onViewRequestSendFile(File file) {
        // Do not allow button actions if there are no remote Peers in the room.
        if (fileTransferService.getTotalPeersInRoom() < 2) {
            String log = context.getString(R.string.warn_no_peer_message);
            toastLog(TAG, context, log);
            return;
        }

        //Check file valid
        if (file != null && file.isFile()) {
            fileTransferView.onPresenterRequestDisplayFilePreview(file.getAbsolutePath());
        } else {
            String log = "Please enter a valid file path";
            toastLog(TAG, context, log);
            return;
        }

        // send file to all peers in room if user do not select any specific peer
        if (selectedPeerIndex == 0) {
            // use service layer to send the file
            fileTransferService.sendFile(null, file);
        } else {
            String remotePeerId = fileTransferService.getPeerByIndex(selectedPeerIndex).getPeerId();
            fileTransferService.sendFile(remotePeerId, file);
        }
    }

    @Override
    public void onViewRequestExit() {
        //process disconnect from room
        fileTransferService.disconnectFromRoom();
        //after disconnected from skylink SDK, UI will be updated latter on onServiceRequestDisconnect
    }

    //----------------------------------------------------------------------------------------------
    // Override methods from BasePresenter for service to call
    // These methods are responsible for processing requests from service
    //----------------------------------------------------------------------------------------------

    @Override
    public void onServiceRequestConnect(boolean isSuccessful) {
        if (isSuccessful) {
            processUpdateStateConnected();
        }
    }

    @Override
    public void onServiceRequestRemotePeerJoin(SkylinkPeer newPeer) {
        // Fill the new peer in button in custom bar
        fileTransferView.onPresenterRequestChangeUiRemotePeerJoin(newPeer,
                fileTransferService.getTotalPeersInRoom() - 2);
    }

    @Override
    public void onServiceRequestRemotePeerLeave(SkylinkPeer remotePeer, int removeIndex) {
        // do not process if the left peer is local peer
        if (removeIndex == -1)
            return;

        // Remove the peer in button in custom bar
        fileTransferView.onPresenterRequestChangeUiRemotePeerLeft(fileTransferService.getPeersList());
    }

    /**
     * process SDK permission
     */
    @Override
    public void onServiceRequestPermissionRequired(PermRequesterInfo info) {
        // delegate to the PermissionUtils to process the permission
        permissionUtils.onPermissionRequiredHandler(info, TAG, context, fileTransferView.onPresenterRequestGetFragmentInstance());
    }

    @Override
    public void onServiceRequestFileTransferPermissionRequest(String remotePeerId, String fileName, boolean isPrivate) {
        // Take note of download file name.
        if (!"".equals(fileName)) {
            SAMPLE_FILE_NAME = fileName;
        }
        //Send false to reject file transfer
        fileTransferService.sendFileTransferPermissionResponse(remotePeerId, Utils.getDownloadedFilePath(), true);
    }

    @Override
    public void onServiceRequestFileReceiveComplete(String remotePeerId, String fileName) {
        SkylinkPeer remotePeer = fileTransferService.getPeerById(remotePeerId);
        fileTransferView.onPresenterRequestFileReceived(remotePeer, fileName);
    }

    @Override
    public void onServiceRequestFileSendComplete(Context context, String remotePeerId, String fileName) {
        fileTransferView.onPresenterRequestFileSent();
    }

    @Override
    public void onServiceRequestFileSendProgress(Context context, String remotePeerId, String fileName, double percentage) {
        fileTransferView.onPresenterRequestFileSendProgress((int) percentage);
    }

    @Override
    public void onServiceRequestFileReceiveProgress(Context context, String remotePeerId, String fileName, double percentage) {
        fileTransferView.onPresenterRequestFileReceiveProgress((int) percentage);
    }

    //----------------------------------------------------------------------------------------------
    // private methods for internal process
    //----------------------------------------------------------------------------------------------

    /**
     * Update UI when connected to room
     */
    private void processUpdateStateConnected() {
        // Update the view into connected state
        fileTransferView.onPresenterRequestUpdateUIConnected(processGetRoomId());
    }

    /**
     * Get the room id info
     */
    private String processGetRoomId() {
        return fileTransferService.getRoomId();
    }

    /**
     * Get the current index of selected peer
     */
    @Override
    public int onViewRequestGetCurrentSelectedPeer() {
        return this.selectedPeerIndex;
    }

    /**
     * Get the specific peer object according to the index
     */
    @Override
    public SkylinkPeer onViewRequestGetPeerByIndex(int index) {
        return fileTransferService.getPeerByIndex(index);
    }

    /**
     * Save the current index of the selected peer
     */
    @Override
    public void onViewRequestSelectedRemotePeer(int index) {
        // check the selected index with the current selectedPeerIndex
        // if it is equal which means user in selects the peer
        if (this.selectedPeerIndex == index) {
            this.selectedPeerIndex = 0;
        } else {
            this.selectedPeerIndex = index;
        }
    }
}
