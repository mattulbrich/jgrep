
package de.matul.jgrep;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

@SuppressWarnings("serial")
public class NoWrapEditorKit extends StyledEditorKit {
    ViewFactory defaultFactory=new NoWrapColumnFactory();
    public ViewFactory getViewFactory() {
        return defaultFactory;
    }
}

class NoWrapParagraphView extends ParagraphView {
    public NoWrapParagraphView(Element elem) {
        super(elem);
    }

    public void layout(int width, int height) {
        super.layout(Short.MAX_VALUE, height);
    }

    public float getMinimumSpan(int axis) {
        return super.getPreferredSpan(axis);
    }
}

class NoWrapColumnFactory implements ViewFactory {
    public View create(Element elem) {
        String kind = elem.getName();
        if (kind != null) {
            if (kind.equals(AbstractDocument.ContentElementName)) {
                return new LabelView(elem);
            } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
                return new NoWrapParagraphView(elem);
            } else if (kind.equals(AbstractDocument.SectionElementName)) {
                return new BoxView(elem, View.Y_AXIS);
            } else if (kind.equals(StyleConstants.ComponentElementName)) {
                return new ComponentView(elem);
            } else if (kind.equals(StyleConstants.IconElementName)) {
                return new IconView(elem);
            }
        }

        // default to text display
        return new LabelView(elem);
    }
}

