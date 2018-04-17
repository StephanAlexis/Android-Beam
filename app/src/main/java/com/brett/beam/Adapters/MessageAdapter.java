package com.brett.beam.Adapters;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Movie;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.brett.beam.R;
import com.brett.beam.models.Messsage;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created by Stephan on 4/15/2018.
 */

public class MessageAdapter extends ArrayAdapter<Messsage> {

    private static class ViewHolder {
        TextView tvExpediteur;
        TextView tvMessage;
        TextView tvDate;
        ImageView ivPhoto;
    }

    public MessageAdapter(Context context, List<Messsage> messages) {
        super(context,  android.R.layout.simple_list_item_1, messages);
    }

    private  ViewHolder remplirViewHolder(TextView tvExpediteur, TextView tvMessage, TextView tvDate)
    {
        ViewHolder vh=new ViewHolder();
        vh.tvExpediteur=tvExpediteur;
        vh.tvMessage=tvMessage;
        vh.tvDate=tvDate;
        return vh;
    }
    @Override
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {

        Messsage message=getItem(position);

        ViewHolder viewHolder;
        if(convertView==null) {
            viewHolder=new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            if(!message.getIsImage()) {
                if (message.getExpediteur().compareTo("self") == 0) {
                    convertView = inflater.inflate(R.layout.envoye, parent, false);
                    viewHolder = remplirViewHolder((TextView) convertView.findViewById(R.id.idDestinataire), viewHolder.tvMessage = (TextView) convertView.findViewById(R.id.leMessageEnvoye), viewHolder.tvDate = (TextView) convertView.findViewById(R.id.messageEnvoyeLe));

                } else {
                    convertView = inflater.inflate(R.layout.recu, parent, false);
                    viewHolder = remplirViewHolder((TextView) convertView.findViewById(R.id.idExpediteur), viewHolder.tvMessage = (TextView) convertView.findViewById(R.id.leMessageRecu), viewHolder.tvDate = (TextView) convertView.findViewById(R.id.messageRecuLe));

                }
            }
            else
            {
                if (message.getExpediteur().compareTo("self") == 0) {
                    convertView = inflater.inflate(R.layout.photoenvoye, parent, false);
                    viewHolder.tvExpediteur=(TextView) convertView.findViewById(R.id.idDestinataireImage);
                    viewHolder.ivPhoto=(ImageView) convertView.findViewById(R.id.laPhotoEnvoye);
                    viewHolder.tvDate=(TextView) convertView.findViewById(R.id.photoEnvoyeLe);
                } else {
                    convertView = inflater.inflate(R.layout.photorecu, parent, false);
                    viewHolder.tvExpediteur=(TextView) convertView.findViewById(R.id.idExpediteurImage);
                    viewHolder.ivPhoto=(ImageView) convertView.findViewById(R.id.laPhotoRecu);
                    viewHolder.tvDate=(TextView) convertView.findViewById(R.id.photoRecuLe);
                }
            }
            convertView.setTag(viewHolder);
        }
        else
            viewHolder=(ViewHolder) convertView.getTag();






        viewHolder.tvExpediteur.setText("Sender: "+message.getExpediteur());
        if(!message.getIsImage())
            viewHolder.tvMessage.setText("Message: \n"+message.getMessage());
        else
            viewHolder.ivPhoto.setImageBitmap(message.getBitmap());
        viewHolder.tvDate.setText("Date: "+message.getLaDate());

        return convertView;
    }


}
