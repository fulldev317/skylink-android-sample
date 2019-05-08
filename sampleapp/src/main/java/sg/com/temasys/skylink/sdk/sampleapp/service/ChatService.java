package sg.com.temasys.skylink.sdk.sampleapp.service;

import android.content.Context;
import android.util.Log;

import sg.com.temasys.skylink.sdk.rtc.SkylinkConfig;
import sg.com.temasys.skylink.sdk.rtc.SkylinkException;
import sg.com.temasys.skylink.sdk.sampleapp.BasePresenter;
import sg.com.temasys.skylink.sdk.sampleapp.chat.ChatContract;
import sg.com.temasys.skylink.sdk.sampleapp.service.model.SkylinkPeer;
import sg.com.temasys.skylink.sdk.sampleapp.utils.Utils;

/**
 * Created by muoi.pham on 20/07/18.
 * This class is responsible for communicating with SkylinkSDK
 */

public class ChatService extends SkylinkCommonService implements ChatContract.Service {

    private final String TAG = ChatService.class.getName();

    public ChatService(Context context) {
        super(context);
    }

    @Override
    public void setPresenter(ChatContract.Presenter presenter) {
        this.presenter = (BasePresenter) presenter;
    }

    /**
     * Sends a user defined message to a specific remote peer or to all remote peers via a server.
     *
     * @param remotePeerId Id of the remote peer to whom we will send a message. Use 'null' if the
     *                     message is to be broadcast to all remote peers in the room.
     * @param message      User defined data
     */
    public void sendServerMessage(String remotePeerId, String message) {
        if (mSkylinkConnection != null) {
            mSkylinkConnection.sendServerMessage(remotePeerId, message);
        }
    }

    /**
     * Sends a user defined message to a specific remote peer or to all remote peers in a direct
     * peer to peer manner.
     *
     * @param remotePeerId Id of the remote peer to whom we will send a message. Use 'null' if the
     *                     message is to be sent to all our remote peers in the room.
     * @param message      User defined data
     * @throws SkylinkException if the system was unable to send the message.
     */
    public void sendP2PMessage(String remotePeerId, String message) {
        if (mSkylinkConnection != null) {
            try {
                mSkylinkConnection.sendP2PMessage(remotePeerId, message);
            } catch (SkylinkException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    /**
     * Sets the specified listeners for message function
     * Message function needs to implement LifeCycleListener, RemotePeerListener, MessagesListener
     */
    @Override
    public void setSkylinkListeners() {
        if (mSkylinkConnection != null) {
            mSkylinkConnection.setLifeCycleListener(this);
            mSkylinkConnection.setRemotePeerListener(this);
            mSkylinkConnection.setMessagesListener(this);
        }
    }

    /**
     * Get the config for message function
     * User can custom message config by using SkylinkConfig
     */
    @Override
    public SkylinkConfig getSkylinkConfig() {
        SkylinkConfig skylinkConfig = new SkylinkConfig();
        // Chat config options can be:
        // NO_AUDIO_NO_VIDEO | AUDIO_ONLY | VIDEO_ONLY | AUDIO_AND_VIDEO
        skylinkConfig.setAudioVideoSendConfig(SkylinkConfig.AudioVideoConfig.NO_AUDIO_NO_VIDEO);
        skylinkConfig.setAudioVideoReceiveConfig(SkylinkConfig.AudioVideoConfig.NO_AUDIO_NO_VIDEO);
        skylinkConfig.setHasPeerMessaging(true);

        // Set the room size
        skylinkConfig.setRoomSize(SkylinkConfig.RoomSize.MEDIUM);

        // Set some common configs.
        Utils.skylinkConfigCommonOptions(skylinkConfig);
        return skylinkConfig;
    }

    /**
     * Get the info of a peer in specific index
     */
    public SkylinkPeer getPeerByIndex(int index) {
        return mPeersList.get(index);
    }
}
