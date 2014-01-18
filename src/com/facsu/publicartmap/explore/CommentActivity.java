package com.facsu.publicartmap.explore;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dennytech.common.adapter.BasicAdapter;
import com.dennytech.common.service.dataservice.mapi.CacheType;
import com.dennytech.common.service.dataservice.mapi.MApiRequest;
import com.dennytech.common.service.dataservice.mapi.MApiRequestHandler;
import com.dennytech.common.service.dataservice.mapi.MApiResponse;
import com.facsu.publicartmap.R;
import com.facsu.publicartmap.app.PMActivity;
import com.facsu.publicartmap.bean.GetCommentsResult;
import com.facsu.publicartmap.bean.User;
import com.facsu.publicartmap.bean.UserComment;
import com.facsu.publicartmap.common.APIRequest;

public class CommentActivity extends PMActivity implements MApiRequestHandler {

	private ListView list;
	private Adapter adapter;

	private MApiRequest request;
	private String artworkID;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_comment);
		setTitle(getString(R.string.title_comment));
		setRightButton(R.drawable.title_comment, new OnClickListener() {

			@Override
			public void onClick(View v) {
				User user = User.read(preferences());
				if (user == null) {
					gotoLogin();
				} else {
					Intent i = new Intent(Intent.ACTION_VIEW, Uri
							.parse("pam://addcomment?artworkid=" + artworkID));
					i.putExtra("location",
							getIntent().getParcelableExtra("location"));
					startActivity(i);
				}
			}
		});
		list = (ListView) findViewById(R.id.list);
		adapter = new Adapter();
		list.setAdapter(adapter);

		artworkID = getIntent().getData().getQueryParameter("id");
	}

	@Override
	protected void onDestroy() {
		if (request != null) {
			mapiService().abort(request, this, true);
		}
		super.onDestroy();
	}

	private void requestData() {
		if (request != null) {
			mapiService().abort(request, this, true);
		}
		request = APIRequest.mapiGet(
				"http://web358082.dnsvhost.com/ACservice/ACService.svc/GetComments/"
						+ artworkID, CacheType.DISABLED,
				GetCommentsResult.class);
		mapiService().exec(request, this);
	}

	class Adapter extends BasicAdapter {

		UserComment[] data;

		@Override
		public int getCount() {
			if (data == null) {
				return 1;
			}
			return data.length == 0 ? 1 : data.length;
		}

		@Override
		public Object getItem(int position) {
			if (data == null) {
				return LOADING;
			}
			return data.length == 0 ? EMPTY : data[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Object item = getItem(position);
			if (item == LOADING) {
				requestData();
				return getLoadingView(getString(R.string.msg_loading_comment),
						parent, convertView);

			} else if (item == EMPTY) {
				return getEmptyView(getString(R.string.msg_empty_comment),
						parent, convertView);
			} else {
				View view = convertView;
				if (!(view instanceof FrameLayout)) {
					view = LayoutInflater.from(CommentActivity.this).inflate(
							R.layout.list_item_comment, null);
				}
				UserComment uc = (UserComment) item;
				TextView name = (TextView) view.findViewById(R.id.comment_name);
				TextView date = (TextView) view.findViewById(R.id.comment_date);
				TextView content = (TextView) view
						.findViewById(R.id.comment_content);
				name.setText(uc.CommentAuthor);
				date.setText(uc.CreationDate);
				content.setText(uc.Content);
				return view;
			}
		}

	}

	@Override
	public void onRequestStart(MApiRequest req) {
	}

	@Override
	public void onRequestProgress(MApiRequest req, int count, int total) {
	}

	@Override
	public void onRequestFinish(MApiRequest req, MApiResponse resp) {
		if (resp.result() instanceof GetCommentsResult) {
			GetCommentsResult result = (GetCommentsResult) resp.result();
			adapter.data = result.result();
			adapter.notifyDataSetChanged();

		} else {
			Toast.makeText(this, getString(R.string.msg_failed_comment),
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onRequestFailed(MApiRequest req, MApiResponse resp) {
		Toast.makeText(this, getString(R.string.msg_failed_comment),
				Toast.LENGTH_SHORT).show();
	}
}
