package com.example.newfinal;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PtaxiAdapter  extends RecyclerView.Adapter<PtaxiViewHolder> {
    private Context mContext;
    private List<Taxitime> mTaxitime= new ArrayList<>();
    PortToServer port;
    Gson gson = new Gson();
    View baseView;

    public PtaxiAdapter(Context mContext, List<Taxitime> time) {
        this.mContext = mContext;
        this.port = new PortToServer("http://143.248.36.38:3000", ((MainActivity)mContext).cookies);
        for(int i=0;i<time.size();i++){
            Taxitime taxiitem= time.get(i);
            if(taxiitem.startplace.equals(Fragment3.startpoint)&&taxiitem.endplace.equals(Fragment3.endpoint)&&Taxi.getTime.equals(taxiitem.date)){
                this.mTaxitime.add(taxiitem);
                System.out.println("condition matched");
            }
            else{
                System.out.println(taxiitem.date+"1");
                System.out.println(Taxi.getTime);
                System.out.println(taxiitem.startplace+"2");
                System.out.println(Fragment3.startpoint);
                System.out.println(taxiitem.endplace+"3");
                System.out.println(Fragment3.endpoint);
            }
        }
    }

    @NonNull
    @Override
    public PtaxiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        baseView = View.inflate(mContext, R.layout.time_data,null);
        PtaxiViewHolder postViewHolder=new PtaxiViewHolder(baseView);
        return postViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final PtaxiViewHolder holder, final int position) {
        Taxitime taxitime= mTaxitime.get(position);
        holder.ivStime.setText(taxitime.stime);
        holder.ivEtime.setText(taxitime.etime);
        holder.ivlimit.setText(taxitime.limit);
        if(holder.ivpeople.getText()==null){
            System.out.println("this is now be filled");
            holder.ivpeople.setText('0');
        }
        holder.ivtaxirow.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new AlertDialog.Builder(mContext)
                        .setTitle("Confirm Message")
                        .setMessage("일정을 삭제하시겠습니까?")
                        .setIcon(android.R.drawable.ic_menu_save)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Taxitime removed = mTaxitime.remove(position);
                                Toast.makeText(mContext.getApplicationContext(), "일정이 삭제되었습니다", Toast.LENGTH_SHORT).show();
                                QueryToServerMongoBuilder builder = new QueryToServerMongoBuilder("madcamp", "taxi");
                                QueryToServerMongo queryS = builder.getQueryD(new JSONArray().put(new BasicDBObject().append("taxi.user", removed.user)));
                                try {
                                    JSONObject obj = port.postToServerV2(queryS);
                                    if (obj.getString("result").equals("OK")){
                                        mTaxitime.clear();
                                        obj = port.postToServerV2(builder.getQueryR(new JSONArray().put(new BasicDBObject())));
                                        if (obj.getString("result").equals("OK")) {
                                            if (obj.getJSONArray("data").length()>0){
                                                JSONArray found = obj.getJSONArray("data");
                                                List<Taxitime> taxitimeList = new ArrayList<>();
                                                for (int i=0; i<found.length(); i++){
                                                    taxitimeList.add(gson.fromJson(found.getJSONObject(i).getString("taxi"), Taxitime.class));
                                                }
                                                System.out.println("ok");
                                                mTaxitime.addAll(taxitimeList);
                                            }
                                        }
                                        notifyDataSetChanged();
                                    }
                                } catch(IOException e){
                                    e.printStackTrace();
                                } catch(JSONException e){
                                    e.printStackTrace();
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        })
                        .show();
                return true;
            }
        });
        holder.iventer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //인원수 추가
                CharSequence currentholder= holder.ivpeople.getText();
                CharSequence currentlimit = holder.ivlimit.getText();
                final int peoplenumber = Integer.parseInt(currentholder.toString());
                int limitnum = Integer.parseInt(currentlimit.toString());
                if(holder.iventer.getText().equals("참여")) {
                    if (peoplenumber >= limitnum) {
                        Toast.makeText(mContext,"정원초과!",Toast.LENGTH_SHORT).show();
                    } else {
                        int innum = peoplenumber + 1;
                        CharSequence added = (CharSequence) Integer.toString(innum);
                        holder.ivpeople.setText(added);
                        holder.iventer.setText("나가기");
                        holder.iventer.setBackground(mContext.getDrawable(R.drawable.layout_bgc));
                    }
                }
                else {
                            int denum = peoplenumber - 1;
                            CharSequence added = (CharSequence) Integer.toString(denum);
                            holder.ivpeople.setText(added);
                            holder.iventer.setText("참여");
                            holder.iventer.setBackground(mContext.getDrawable(R.drawable.layout_bg));
                }
            }
        });
    }
    @Override
    public int getItemCount() {
        if(mTaxitime==null){
            return 0;
        }
        return mTaxitime.size();
    }

}
