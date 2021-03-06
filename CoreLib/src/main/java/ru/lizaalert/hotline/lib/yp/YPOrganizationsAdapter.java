/*
    Copyright (c) 2014 Denis Volyntsev <fortun777@gmail.com>
    Copyright (c) 2014 Other contributors as noted in the AUTHORS file.

    Этот файл является частью приложения "Мобильное рабочее место оператора
    Горячей линии по пропавшим детям".

    Данная лицензия разрешает лицам, получившим копию "Мобильного рабочего
    места оператора Горячей линии по пропавшим детям" и сопутствующей
    документации (в дальнейшем именуемыми «Программное Обеспечение»),
    безвозмездно использовать Программное Обеспечение без ограничений, включая
    неограниченное право на использование, копирование, изменение, добавление,
    публикацию, распространение, сублицензирование и/или продажу копий
    Программного Обеспечения, также как и лицам, которым предоставляется данное
    Программное Обеспечение, при соблюдении следующих условий:

    Указанное выше уведомление об авторском праве и данные условия должны быть
    включены во все копии или значимые части данного Программного Обеспечения.

    ДАННОЕ ПРОГРАММНОЕ ОБЕСПЕЧЕНИЕ ПРЕДОСТАВЛЯЕТСЯ «КАК ЕСТЬ», БЕЗ КАКИХ-ЛИБО
    ГАРАНТИЙ, ЯВНО ВЫРАЖЕННЫХ ИЛИ ПОДРАЗУМЕВАЕМЫХ, ВКЛЮЧАЯ, НО НЕ
    ОГРАНИЧИВАЯСЬ ГАРАНТИЯМИ ТОВАРНОЙ ПРИГОДНОСТИ, СООТВЕТСТВИЯ ПО ЕГО
    КОНКРЕТНОМУ НАЗНАЧЕНИЮ И ОТСУТСТВИЯ НАРУШЕНИЙ ПРАВ. НИ В КАКОМ СЛУЧАЕ
    АВТОРЫ ИЛИ ПРАВООБЛАДАТЕЛИ НЕ НЕСУТ ОТВЕТСТВЕННОСТИ ПО ИСКАМ О ВОЗМЕЩЕНИИ
    УЩЕРБА, УБЫТКОВ ИЛИ ДРУГИХ ТРЕБОВАНИЙ ПО ДЕЙСТВУЮЩИМ КОНТРАКТАМ, ДЕЛИКТАМ
    ИЛИ ИНОМУ, ВОЗНИКШИМ ИЗ, ИМЕЮЩИМ ПРИЧИНОЙ ИЛИ СВЯЗАННЫМ С ПРОГРАММНЫМ
    ОБЕСПЕЧЕНИЕМ ИЛИ ИСПОЛЬЗОВАНИЕМ ПРОГРАММНОГО ОБЕСПЕЧЕНИЯ ИЛИ ИНЫМИ
    ДЕЙСТВИЯМИ С ПРОГРАММНЫМ ОБЕСПЕЧЕНИЕМ.

    Кроме содержимого в этом уведомлении, ни название "Горячей линии по
    пропавшим детям", ни название "Добровольного поискового отряда "Лиза
    Алерт", ни имена вышеуказанных держателей авторских прав не должно быть
    использовано в рекламе или иным способом, чтобы увеличивать продажу,
    использование или другие работы в этом Программном обеспечении без
    предшествующего письменного разрешения.

    Permission is hereby granted, free of charge, to any person obtaining a
    copy of this software and associated documentation files (the "Software"),
    to deal in the Software without restriction, including without limitation
    the rights to use, copy, modify, merge, publish, distribute, sublicense,
    and/or sell copies of the Software, and to permit persons to whom the
    Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in
    all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
    THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
    FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
    DEALINGS IN THE SOFTWARE.

    Except as contained in this notice, the name of Liza Alert or the name of
    Liza Alerts's hotline department or the name(s) the above copyright holders
    shall not be used in advertising or otherwise to promote the sale, use or
    other dealings in this Software without prior written authorization.
 */

package ru.lizaalert.hotline.lib.yp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.TextAppearanceSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;
import ru.lizaalert.hotline.lib.R;


public class YPOrganizationsAdapter extends RealmBaseAdapter<YPEntry> implements ListAdapter, SectionIndexer {
    private static final String TAG = "8800";
    private static final Character TMP_CHAR = '\u00A0';

    private ViewHolder viewHolder;

    private int viewId;
    private int sectionId;
    private int organizationId;
    private int phonesId;
    private int descriptionId;
    private int addressId;
    private int emailId;
    private RealmResults<YPEntry> realmResults;

    Object[] sections;
    Integer[] positions;

    public YPOrganizationsAdapter(Context context,
                                  RealmResults<YPEntry> realmResults,
                                  boolean automaticUpdate,
                                  int viewId,
                                  int sectionId,
                                  int organizationId,
                                  int phonesId,
                                  int addressId,
                                  int emailId,
                                  int descriptionId) {
        super(context, realmResults, automaticUpdate);
        this.viewId = viewId;
        this.sectionId = sectionId;
        this.organizationId = organizationId;
        this.phonesId = phonesId;
        this.addressId = addressId;
        this.emailId = emailId;
        this.descriptionId = descriptionId;
        this.realmResults = realmResults;

        makeSectionsList();
    }

    public void makeSectionsList() {
        ArrayList<String> sectionsList = new ArrayList<>();
        ArrayList<Integer> sectionsPositions = new ArrayList<>();
        String section = "";

        for (int i = 0; i < realmResults.size(); i++) {
            YPEntry u = realmResults.get(i);
            if (!u.getSection().equals(section)) {
                section = u.getSection();
                sectionsList.add(section);
                sectionsPositions.add(i);
            }
        }

        sections = sectionsList.toArray();
        positions = sectionsPositions.toArray(new Integer[sectionsPositions.size()]);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(viewId, parent, false);

            // well set up the ViewHolder
            viewHolder = new ViewHolder();
            viewHolder.section = (TextView) convertView.findViewById(sectionId);
            viewHolder.organizationName = (TextView) convertView.findViewById(organizationId);
            viewHolder.phones = (TextView) convertView.findViewById(phonesId);
            viewHolder.description = (TextView) convertView.findViewById(descriptionId);
            viewHolder.email = (TextView) convertView.findViewById(emailId);
            viewHolder.address = (TextView) convertView.findViewById(addressId);

            // store the holder with the view.
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // object item based on the position
        YPEntry item = getItem(position);

        // assign values if the object is not null
        if (item != null && item.getName() != null) {
            boolean separator = false;
            if (position == 0) {
                separator = true;
            } else {
                YPEntry prev = getItem(position - 1);
                if (prev != null && !prev.getSection().equals(item.getSection())) {
                    separator = true;
                }
            }

            if (separator) {
                viewHolder.section.setText(item.getSection());
                viewHolder.section.setVisibility(View.VISIBLE);
            } else {
                viewHolder.section.setVisibility(View.GONE);
            }

            // get the TextView from the ViewHolder and then set the text (item name) and tag (item ID) values
            viewHolder.organizationName.setText(item.getName());

            StringBuffer p = new StringBuffer(
                    item.getPhone()
                            .replace(";", "\n")
                            .replace("'+", "+"));
            if (p.length() > 0 && p.charAt(0) == '\'') {
                p.deleteCharAt(0);
            }

            // здесь будет происходить следующее:
            // 1. найдем все телефонные номера с помощью Linkify и превратим в ссылки
            // 2. в тексте, который стал ссылкой, найдем все isWhitespace-символы и заменим в исходной строке (!) эти символы на TMP_CHAR
            // 3. из исходной строки удалим все TMP_CHAR (не признает
            // 4. опять найдем все телефонные номера с помощью Linkify
            Spannable st = new SpannableString(p);
            Linkify.addLinks(st, Linkify.PHONE_NUMBERS);

            URLSpan[] spans = st.getSpans(0, st.length() , URLSpan.class);
            for (URLSpan u : spans) {

                int start = st.getSpanStart(u);
                int end = st.getSpanEnd(u);

                for (int i = end - 1; i >= start; i--) {
                    if (Character.isWhitespace(st.subSequence(i, i + 1).charAt(0))) {
                        p.setCharAt(i, TMP_CHAR);
                    }

                }
            }

            st = new SpannableString(p.toString().replace(String.valueOf(TMP_CHAR), ""));
            Linkify.addLinks(st, Linkify.PHONE_NUMBERS);

            spans = st.getSpans(0, st.length() , URLSpan.class);
            for (URLSpan u : spans) {

                int start = st.getSpanStart(u);
                int end = st.getSpanEnd(u);
                if (end > start) {
                    st.setSpan(new TextAppearanceSpan(context, R.style.PhoneNumberTextViewStyle), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }

            viewHolder.phones.setText(st, TextView.BufferType.SPANNABLE);
            viewHolder.phones.setMovementMethod(LinkMovementMethod.getInstance());

            if (item.getDescription().isEmpty()) {
                viewHolder.description.setVisibility(View.GONE);
            } else {
                viewHolder.description.setVisibility(View.VISIBLE);
                viewHolder.description.setText(item.getDescription());
            }

            if (item.getEmail().isEmpty()) {
                viewHolder.email.setVisibility(View.GONE);
            } else {
                viewHolder.email.setVisibility(View.VISIBLE);
                viewHolder.email.setText(item.getEmail());
            }

            if (item.getAddress().isEmpty()) {
                viewHolder.address.setVisibility(View.GONE);
            } else {
                viewHolder.address.setVisibility(View.VISIBLE);
                viewHolder.address.setText(item.getAddress());
                viewHolder.address.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openMap(v);

                    }
                });
            }



//            Log.i(LOG_TAG, "display item " + position + " " + item.name);
        }

        return convertView;
    }

    private void openMap (View v) {

        String address = ((TextView)v).getText().toString();
        String map = "geo:0,0?q=" + address;
        Uri uri = Uri.parse(map);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }

    @Override
    public Object[] getSections() {
        Log.d(TAG, "getSections");
        return sections;
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        if (sectionIndex < positions.length) {
            return positions[sectionIndex];
        }
        return 0;
    }

    @Override
    public int getSectionForPosition(int position) {
        for (int i = 0; i < positions.length; i++) {
            if (positions[i] > position) {
                return i - 1;
            }
        }
        return positions.length - 1;
    }

    private static class ViewHolder {
        public TextView section;
        public TextView organizationName;
        public TextView phones;
        public TextView description;
        public TextView email;
        public TextView address;
    }
}
