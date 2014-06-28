package com.will.watcher.handlebar;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.will.watcher.util.JsonUtil;
import com.will.watcher.util.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class HandlebarHelpers {

    @Autowired
    private Handlebar handlebar;

    @Autowired
    private JsonUtil jsonUtil;

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.###");

    private static final Splitter splitter = Splitter.on(",");

    @PostConstruct
    public void init() {
        this.handlebar.registerHelper("json", new Helper() {
            public CharSequence apply(Object context, Options options) throws IOException {
                return jsonUtil.toJson(context);
            }
        });
        this.handlebar.registerHelper("match", new Helper<String>() {
            public CharSequence apply(String regEx, Options options) throws IOException {
                Pattern pat = Pattern.compile(regEx);
                Matcher mat = pat.matcher((String) options.param(0));
                if (mat.find()) {
                    return options.fn();
                }
                return options.inverse();
            }
        });
        this.handlebar.registerHelper("gt", new Helper() {
            public CharSequence apply(Object source, Options options)
                    throws IOException {
                long _source;
                if ((source instanceof Long)) {
                    _source = ((Long) source).longValue();
                } else {
                    if ((source instanceof Integer))
                        _source = ((Integer) source).intValue();
                    else {
                        _source = Long.parseLong((String) source);
                    }
                }
                if (_source > ((Integer) options.param(0)).intValue()) {
                    return options.fn();
                }
                return options.inverse();
            }
        });
        this.handlebar.registerHelper("mod", new Helper<Integer>() {
            public CharSequence apply(Integer source, Options options) throws IOException {
                if ((source.intValue() + 1) % ((Integer) options.param(0)).intValue() == 0) {
                    return options.fn();
                }
                return options.inverse();
            }
        });
        this.handlebar.registerHelper("size", new Helper() {
            public CharSequence apply(Object context, Options options) throws IOException {
                if (context == null) return "0";
                if ((context instanceof Collection)) return String.valueOf(((Collection) context).size());
                if ((context instanceof Map)) return String.valueOf(((Map) context).size());
                return "0";
            }
        });
        this.handlebar.registerHelper("equals", new Helper<Object>() {
            public CharSequence apply(Object source, Options options) throws IOException {
                if (Objects.equal(String.valueOf(source), String.valueOf(options.param(0)))) {
                    return options.fn();
                }
                return options.inverse();
            }
        });
        this.handlebar.registerHelper("formatDate", new Helper<Date>() {
            Map<String, SimpleDateFormat> sdfMap = ImmutableMap.of("gmt", new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy"), "day", new SimpleDateFormat("yyyy-MM-dd"), "default", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

            public CharSequence apply(Date date, Options options)
                    throws IOException {
                if (date == null) {
                    return "";
                }
                String format = (String) options.param(0, "default");
                if (format.equals("ut")) {
                    return Long.toString(date.getTime());
                }
                if (!this.sdfMap.containsKey(format)) {
                    this.sdfMap.put(format, new SimpleDateFormat(format));
                }
                return ((SimpleDateFormat) this.sdfMap.get(format)).format(date);
            }
        });
        this.handlebar.registerHelper("formatPrice", new Helper<Number>() {
            public CharSequence apply(Number price, Options options) throws IOException {
                return NumberUtils.formatPrice(price);
            }
        });
        this.handlebar.registerHelper("innerStyle", new Helper() {
            public CharSequence apply(Object context, Options options) throws IOException {
                if (context == null) {
                    return "";
                }

                StringBuilder ret = new StringBuilder();

                String[] styles = ((String) context).split(";");
                for (String style : styles) {
                    String key = style.split(":")[0];
                    if (key.endsWith("radius")) {
                        ret.append(style).append(";");
                    }
                }
                return ret;
            }
        });
        this.handlebar.registerHelper("cIndex", new Helper<Integer>() {
            public CharSequence apply(Integer context, Options options) throws IOException {
                return "" + (char) (context.intValue() + 65);
            }
        });
        this.handlebar.registerHelper("formatRate", new Helper<Double>() {
            public CharSequence apply(Double rate, Options options) throws IOException {
                return rate == null ? "" : HandlebarHelpers.DECIMAL_FORMAT.format(rate.doubleValue() / 1000.0D);
            }
        });
        this.handlebar.registerHelper("formatIntegerRate", new Helper<Integer>() {
            public CharSequence apply(Integer rate, Options options) throws IOException {
                return rate == null ? "" : HandlebarHelpers.DECIMAL_FORMAT.format(rate.intValue() / 1000.0D);
            }
        });
        this.handlebar.registerHelper("of", new Helper() {
            public CharSequence apply(Object source, Options options) throws IOException {
                if (source == null) {
                    return options.inverse();
                }

                String _source = source.toString();
                String param = (String) options.param(0);
                if (Strings.isNullOrEmpty(param)) {
                    return options.inverse();
                }

                List targets = HandlebarHelpers.splitter.splitToList(param);
                if (targets.contains(_source)) {
                    return options.fn();
                }
                return options.inverse();
            }
        });
        this.handlebar.registerHelper("add", new Helper() {
            public CharSequence apply(Object source, Options options) throws IOException {
                Object param = options.param(0);

                if ((source == null) && (param == null)) {
                    return "";
                }

                if (source == null) {
                    return param.toString();
                }

                if (param == null) {
                    return source.toString();
                }

                if ((source instanceof Double)) {
                    Double first = (Double) source;
                    Double second = (Double) param;
                    return String.valueOf(first.doubleValue() + second.doubleValue());
                }

                if ((source instanceof Integer)) {
                    Integer first = (Integer) source;
                    Integer second = (Integer) param;
                    return String.valueOf(first.intValue() + second.intValue());
                }

                if ((source instanceof Long)) {
                    Long first = (Long) source;
                    Long second = (Long) param;
                    return String.valueOf(first.longValue() + second.longValue());
                }

                if ((source instanceof String)) {
                    Integer first = Integer.valueOf(Integer.parseInt(source.toString()));
                    Integer second = Integer.valueOf(Integer.parseInt(param.toString()));
                    return String.valueOf(first.intValue() + second.intValue());
                }

                throw new IllegalStateException("incorrect.type");
            }
        });
        this.handlebar.registerHelper("rget", new Helper() {
            private final Random random = new Random(System.currentTimeMillis());

            public CharSequence apply(Object context, Options options)
                    throws IOException {
                List list;
                if ((context instanceof List))
                    list = (List) context;
                else {
                    list = Splitter.on(",").trimResults().splitToList(String.valueOf(context));
                }
                if ((list == null) || (list.isEmpty())) {
                    return null;
                }
                return list.get(this.random.nextInt(list.size())).toString();
            }
        });
    }
}
