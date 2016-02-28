package hu.tilos.radio.backend.util;

import hu.tilos.radio.backend.converters.FairEnoughHtmlSanitizer;
import hu.tilos.radio.backend.converters.HTMLSanitizer;
import hu.tilos.radio.backend.tag.TagUtil;
import org.pegdown.Extensions;
import org.pegdown.LinkRenderer;
import org.pegdown.PegDownProcessor;
import org.pegdown.ast.AutoLinkNode;
import org.pegdown.ast.ExpLinkNode;

import javax.inject.Inject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextConverter {

    private static final Pattern YOUTUBE = Pattern.compile("^\\s*(?![\"\\[\\(])(?:https?:)?(//)?(?:www\\.)?(?:youtube\\.com|youtu\\.be)\\S+\\s*$", Pattern.MULTILINE);

    @Inject
    HTMLSanitizer liberalSanitizer;

    @Inject
    TagUtil tagUtil;

    @Inject
    FairEnoughHtmlSanitizer fairSanitizer;

    private PegDownProcessor pegdown;

    public TextConverter() {
        this.pegdown = new PegDownProcessor(Extensions.HARDWRAPS | Extensions.AUTOLINKS);
    }

    public String format(String type, String content) {
        if (type == null || content == null) {
            return content;
        }
        if (type.equals("default") || type.equals("legacy") || type.equals("normal")) {
            return tagUtil.replaceToHtml(liberalSanitizer.clean(content));
        } else if (type.equals("markdown")) {
                content = fairSanitizer.clean(content);
                String youtubized = youtubize(content);
                String cleanAt = youtubized.replaceAll("&#64;", "@");
                String tagged = tagUtil.htmlize(cleanAt);
                return parseMarkdown(tagged);

        }
        throw new IllegalArgumentException("Unkown content type: " + type);
    }

    protected String parseMarkdown(String tagged) {
        return pegdown.markdownToHtml(tagged, new LinkRenderer() {
            @Override
            public Rendering render(ExpLinkNode node, String text) {
                Rendering render = super.render(node, text);
                if (node.url.startsWith("http://tilos.hu") || node.url.startsWith("https://tilos.hu")) {
                    render.withAttribute("target", "_self");
                }
                return render;
            }

            @Override
            public Rendering render(AutoLinkNode node) {
                Rendering render = super.render(node);
                if (node.getText().startsWith("http://tilos.hu") || node.getText().startsWith("https://tilos.hu")) {
                    render.withAttribute("target", "_self");
                }
                return render;
            }
        });
    }

    public String youtubize(String str) {
        Matcher m = YOUTUBE.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String url = m.group(0).replace("watch?v=", "embed/").replace("watch?v&#61;", "embed/");
            m.appendReplacement(sb, "<iframe width=\"420\" height=\"315\" src=\"" + url + "\" frameborder=\"0\" allowfullscreen></iframe>");
        }
        m.appendTail(sb);
        return sb.toString();
    }


}
