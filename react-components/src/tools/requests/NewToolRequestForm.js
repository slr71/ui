/**
 *  @author sriram
 *
 **/

import React, { Component } from "react";

import { Field, Form, Formik } from "formik";
import { injectIntl } from "react-intl";

import constants from "../../constants";
import intlData from "../../tools/messages";
import {
    DEDialogHeader,
    formatMessage,
    FormMultilineTextField,
    FormTextField,
    getMessage,
    withI18N,
} from "@cyverse-de/ui-lib";
import Dialog from "@material-ui/core/Dialog";
import DialogContent from "@material-ui/core/DialogContent";
import Button from "@material-ui/core/Button";

/**
 *
 * A form to collect information about users tool requests.
 *
 */
class NewToolRequestForm extends Component {
    constructor(props) {
        super(props);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleClose = this.handleClose.bind(this);
        this.validateUrl = this.validateUrl.bind(this);
    }

    handleSubmit(values, actions) {
        //test_data_path and cmd_line are required by service. Most of the times those info. can be
        // found in the source repo. So copy the source url to those fields if not provided by user.
        if (!values.test_data_path) {
            values.test_data_path = values.source_url;
        }
        if (!values.cmd_line) {
            values.cmd_line = values.source_url;
        }
        actions.setSubmitting(true);
        this.props.presenter.submitRequest(
            values,
            () => {
                actions.setSubmitting(false);
                this.handleClose();
            },
            (httpStatusCode, errorMessage) => {
                actions.setSubmitting(false);
                actions.setStatus({ success: false, errorMessage });
            }
        );
    }

    handleClose() {
        this.props.presenter.onToolRequestDialogClose();
    }

    validateUrl(value) {
        let error;
        if (!constants.URL_REGEX.test(value)) {
            error = getMessage("validationErrMsgURL");
        }
        return error;
    }

    render() {
        const { intl, dialogOpen } = this.props;
        return (
            <Dialog open={dialogOpen} disableBackdropClick disableEscapeKeyDown>
                <DEDialogHeader
                    heading={intl.formatMessage({
                        id: "newToolRequestDialogHeading",
                    })}
                    onClose={this.handleClose}
                />
                <DialogContent>
                    <Formik
                        enableReinitialize={true}
                        onSubmit={this.handleSubmit}
                        render={({ errors, status, touched, isSubmitting }) => (
                            <Form>
                                <Field
                                    name="name"
                                    label={getMessage("toolNameLabel")}
                                    required={true}
                                    margin="dense"
                                    component={FormTextField}
                                />
                                <Field
                                    name="description"
                                    label={getMessage("toolDescLabel")}
                                    required={true}
                                    margin="dense"
                                    component={FormMultilineTextField}
                                />
                                <Field
                                    name="source_url"
                                    label={getMessage("toolSrcLinkLabel")}
                                    required={true}
                                    margin="dense"
                                    validate={this.validateUrl}
                                    component={FormTextField}
                                />
                                <Field
                                    name="version"
                                    label={getMessage("toolVersionLabel")}
                                    required={true}
                                    margin="dense"
                                    component={FormTextField}
                                />
                                <Field
                                    name="documentation_url"
                                    label={getMessage("toolDocumentationLabel")}
                                    required={true}
                                    margin="dense"
                                    validate={this.validateUrl}
                                    component={FormTextField}
                                />
                                <Field
                                    name="cmd_line"
                                    label={getMessage("toolInstructionsLabel")}
                                    required={false}
                                    margin="dense"
                                    component={FormMultilineTextField}
                                />
                                <Field
                                    name="test_data_path"
                                    label={getMessage("toolTestDataLabel")}
                                    required={false}
                                    margin="dense"
                                    component={FormTextField}
                                />
                                <Button
                                    style={{ float: "right" }}
                                    variant="contained"
                                    color="primary"
                                    type="submit"
                                    aria-label={formatMessage(intl, "submit")}
                                    disabled={isSubmitting}
                                >
                                    {getMessage("submit")}
                                </Button>
                            </Form>
                        )}
                    />
                </DialogContent>
            </Dialog>
        );
    }
}

export default withI18N(injectIntl(NewToolRequestForm), intlData);
