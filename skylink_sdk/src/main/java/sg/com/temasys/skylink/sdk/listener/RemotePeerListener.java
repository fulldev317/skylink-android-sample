package sg.com.temasys.skylink.sdk.listener;

import android.graphics.Point;
import android.opengl.GLSurfaceView;

/**
 * Listener comprises of callbacks related to the remote peers' activities.
 */
public interface RemotePeerListener {

    /**
     * This is triggered when a new peer joins the room.
     *
     * @param peerId   The id of the peer
     * @param userData User defined data relating to the peer. May be a
     *                 'java.lang.String', 'org.json.JSONObject' or
     *                 'org.json.JSONArray'.
     */
    public void onPeerJoin(String peerId, Object userData);

    /**
     * The is triggered upon receiving the video stream of the peer if the
     * connection is configured to have a video call.
     *
     * @param peerId    The id of the peer
     * @param videoView Video of the peer
     * @param size      Size of the peer video frame
     */
    public void onGetPeerMedia(String peerId, GLSurfaceView videoView,
                               Point size);

    /**
     * This is triggered when an update is received in the user defined data
     * of a peer.
     *
     * @param peerId   The id of the peer
     * @param userData User defined data relating to the peer. May be a
     *                 'java.lang.String', 'org.json.JSONObject' or
     *                 'org.json.JSONArray'.
     */
    public void onUserData(String peerId, Object userData);

    /**
     * This is triggered when the underlying data connection is established
     * between two peers and is ready to send and receive peer messages and
     * files between them.
     *
     * @param peerId The id of the peer
     */
    public void onOpenDataConnection(String peerId);

    /**
     * This is triggered when a peer leaves the room.
     *
     * @param peerId  The id of the peer
     * @param message Message specifying the possible reason for leaving the
     *                room
     */
    public void onPeerLeave(String peerId, String message);

}
