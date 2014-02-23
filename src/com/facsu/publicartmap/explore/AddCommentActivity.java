package com.facsu.publicartmap.explore;

import java.util.HashMap;
import java.util.Map;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.dennytech.common.service.dataservice.mapi.MApiRequest;
import com.dennytech.common.service.dataservice.mapi.MApiRequestHandler;
import com.dennytech.common.service.dataservice.mapi.MApiResponse;
import com.facsu.publicartmap.R;
import com.facsu.publicartmap.app.PMActivity;
import com.facsu.publicartmap.app.PMApplication;
import com.facsu.publicartmap.bean.AddCommentResult;
import com.facsu.publicartmap.bean.Location;
import com.facsu.publicartmap.bean.User;
import com.facsu.publicartmap.common.APIRequest;

public class AddCommentActivity extends PMActivity implements
		MApiRequestHandler {

	private TextView addressTv;
	private EditText input;
	private View agreenment;

	private Location location;
	private String artworkID;

	private MApiRequest request;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_add_comment);
		setTitle(getString(R.string.title_share));

		location = getIntent().getParcelableExtra("location");
		if (location == null) {
			location = PMApplication.instance().myLocation();
		}

		artworkID = getIntent().getData().getQueryParameter("artworkid");
		addressTv = (TextView) findViewById(R.id.share_location);
		if (location != null) {
			addressTv.setText(location.address);
		}
		input = (EditText) findViewById(R.id.input);
		agreenment = findViewById(R.id.agreenment);
		agreenment.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showDialog(getString(R.string.app_name),
						getString(R.string.agreement),
						getString(R.string.text_agreen), null,
						getString(R.string.text_reject),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								finish();
							}
						}, new DialogInterface.OnCancelListener() {

							@Override
							public void onCancel(DialogInterface dialog) {
								finish();
							}
						});
			}
		});
		setRightButton(R.drawable.title_send, new OnClickListener() {

			@Override
			public void onClick(View v) {
				requestAddComment(input.getText().toString().trim(), artworkID);
			}
		});
	}

	private void requestAddComment(String content, String aid) {
		if (request != null) {
			mapiService().abort(request, this, true);
		}
		User user = User.read(preferences());
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("UserID", user.UID);
		map.put("ArtworkID", aid);
		map.put("Content", content);
		map.put("Status", "UA");
		map.put("Type", "Normal");
		map.put("CommentAuthor", user.UserName);
		request = APIRequest
				.mapiPostJson(
						"http://web358082.dnsvhost.com/ACservice/ACService.svc/AddComment",
						AddCommentResult.class, map);
		mapiService().exec(request, this);
		showProgressDialog(getString(R.string.msg_submiting));
	}

	@Override
	public void onRequestStart(MApiRequest req) {
	}

	@Override
	public void onRequestProgress(MApiRequest req, int count, int total) {
	}

	@Override
	public void onRequestFinish(MApiRequest req, MApiResponse resp) {
		dismissDialog();
		if (resp.result() instanceof AddCommentResult) {
			AddCommentResult result = (AddCommentResult) resp.result();
			if (!result.AddCommentResult.hasError()) {
				showDialog(getString(R.string.app_name),
						getString(R.string.msg_add_comment_success),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								finish();
							}
						});
			} else {
				showDialog(getString(R.string.app_name),
						result.AddCommentResult.ErrorDesc, null);
			}
		}

	}

	@Override
	public void onRequestFailed(MApiRequest req, MApiResponse resp) {
		dismissDialog();
		showDialog(getString(R.string.app_name), resp.message().getErrorMsg(),
				null);
	}

}
