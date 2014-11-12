/*
    Copyright (c) 2014 Igor Tseglevskiy <igor.tseglevskiy@gmail.com>
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

package ru.lizaalert.hotline.yp;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import org.xmlpull.v1.XmlPullParserException;
import ru.lizaalert.hotline.SpreadsheetXmlParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class YellowPagesLoader {
    private static YellowPagesLoader instance;
    private static Context context;

    private final String YELLOW_PAGES_KEY = "18WABg03Ja4dJHJxVMqWBeEfFYs23D3ArCEuYgQGpk7s";
    private final String YELLOW_PAGES_URL = "http://spreadsheets.google.com/feeds/list/" + YELLOW_PAGES_KEY + "/od6/public/values";

    private static final String TAG = "8800";

    private Realm realm;
    private boolean success = false;
    private AsyncTask<Void, Void, Void> task;


    public static synchronized YellowPagesLoader getInstance(Context c) {
        if (instance == null) {
            context = c;
            instance = new YellowPagesLoader();
        }
        return instance;
    }

    /**
     * Fetchs yellow pages from server.
     * <p/>
     * On load writes data to file and displays it if nothing has been displayed yet
     */
    public void fetchDataAsync() {
        // загружаем данные за время жизни приложения только один раз
        if (!success && (task == null || task.getStatus() == AsyncTask.Status.FINISHED)) {

            task = new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    fetchData();
                    return null;
                }

            };
            task.execute();
        }
    }

    /**
     * Loads data from server and writes it to disk
     *
     * @return
     */
    private void fetchData() {
        String xml;
        List<SpreadsheetXmlParser.Entry> entries = null;

        try {
            URL url = new URL(YELLOW_PAGES_URL);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            xml = inputStreamToString(in);

            in.close();
            urlConnection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (xml == null || xml.length() < 1) {
            Log.d(TAG, "xml is empty");
            return;
        }
        Log.d(TAG, xml);

        SpreadsheetXmlParser parser = SpreadsheetXmlParser.getInstance();

        /*
        entries = new ArrayList<>();
        entries.add(new SpreadsheetXmlParser.Entry("Москва", "Что-то", "1235", "непонятное"));
        */

        try {
            entries = parser.parse(xml);

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (entries == null || entries.size() < 1)
            return;

        realm = Realm.getInstance(context, false);
        RealmResults<YPRegion> allRegions = realm.where(YPRegion.class).findAll();
        RealmResults<YPEntry> allEntries = realm.where(YPEntry.class).findAll();
        realm.beginTransaction();

        allRegions.clear();
        allEntries.clear();

        for (SpreadsheetXmlParser.Entry e : entries) {
            YPRegion region;

            RealmQuery<YPRegion> query = realm.where(YPRegion.class).equalTo("region", e.region);
            if (query.count() > 0) {
                region = query.findFirst();
            } else {
                region = realm.createObject(YPRegion.class);
                region.setRegion(e.region);
            }

            YPEntry entry = realm.createObject(YPEntry.class);
            entry.setRegion(region);
            entry.setName(e.name);
            entry.setPhone(e.phone);
            entry.setDescription(e.description);
        }

        realm.commitTransaction();
        realm.refresh();

        success = true;
    }

    private String inputStreamToString(InputStream inputStream) throws IOException {
        StringBuilder sbuilder;
        BufferedReader input = null;
        try {
            input = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            sbuilder = new StringBuilder();
            String str = input.readLine();

            while (str != null) {
                sbuilder.append(str);
                str = input.readLine();
                if (str != null) {
                    sbuilder.append("\n");
                }
            }

            return sbuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            inputStream.close();
            if (input != null)
                input.close();
        }
        return null;
    }

}
